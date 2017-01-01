import { Component, OnInit } from '@angular/core';
import 'rxjs/add/operator/map'
import { Observable, Subscription } from 'rxjs';
import { Http } from '@angular/http';

@Component({
  selector: 'app-jobprogress',
  templateUrl: './jobprogress.component.html',
  styleUrls: ['./jobprogress.component.css']
})
export class JobprogressComponent implements OnInit {

  private jobProgress: any = { workerProgresses: [], problemInput: {} } ;

  private SockJS : any = require('sockjs-client');

  private Stomp: any = require('stompjs');

  private stompClient: any;

  private runningTime: number;

  private rtime: Observable<number>;

  constructor(private http: Http) {
    let me : JobprogressComponent = this;
    let lastTime = 0;
    this.rtime = Observable.timer(1000, 1000).map(f => {
      if (!me.jobProgress.problemInput.startTime) {
        return  0;
      }
      if (me.jobProgress.problemInput.endTime) {
        let milliSeconds = (this.jobProgress.problemInput.endTime.getTime()
                            - (this.jobProgress.problemInput.startTime.getTime()));
        return Math.floor(milliSeconds / 1000);
      }
      else if (me.jobProgress.problemInput.answer == "UNKOWN") {
        //
        // Yeah hack.
        //
        let milliSeconds = ((new Date()).getTime()
                             - (this.jobProgress.problemInput.startTime.getTime()));
        lastTime = Math.floor(milliSeconds / 1000);
        return lastTime;
      }
      return lastTime;
    });
  }

  private processFunction(message: any): void {
    let update: any = JSON.parse(message.body);
    if (update.workerName == "MASTER") {
        // HACK no global stats available
      if (update.answerUpdate) {
        this.jobProgress.problemInput.answer = update.answerUpdate;
      }
      if (update.solvedAssignmentsUpdate != "") {
        this.jobProgress.problemInput.solvedAssignments = update.solvedAssignmentsUpdate;
      }
    } else {
      if (update.statUpdate != "") {
        this.jobProgress[update.workerName].statUpdate = update.statUpdate;
      }
      if (update.solvedAssignmentsUpdate != "") {
        this.jobProgress[update.workerName].solvedAssignmentsUpdate = update.solvedAssignmentsUpdate;
      }
    }
    console.info("Recieved " + message.body);
  }

  ngOnInit() {
      let me: JobprogressComponent = this;
      this.http.get('/api/jobprogress')
               .map(response =>  <any> response.json())
               .subscribe(
                 result => {
                   for (var workerProgress in result.workerProgresses) {
                     let item: any = result.workerProgresses[workerProgress];
                     let jp : any = {
                       workerName   : item.workerName,
                       answerUpdate : item.answerUpdate,
                       statUpdate   : item.statUpdate,
                       solvedAssignmentsUpdate: item.solvedAssignmentsUpdate
                     };
                     me.jobProgress[item.workerName] = jp;
                     me.jobProgress.workerProgresses.push(jp);
                   }
                   me.jobProgress.problemInput = result;

                   //
                   // Bas: Yeah I know hack ..
                   //
                   let startDate = new Date(result.startTime[0],
                                            result.startTime[1] -1,
                                            result.startTime[2],
                                            result.startTime[3],
                                            result.startTime[4],
                                            result.startTime[5]);
                   me.jobProgress.problemInput.startTime = startDate;

                   //
                   // Set end date if so ..
                   //
                   if (result.endTime) {
                     let endDate = new Date(result.endTime[0],
                                            result.endTime[1] -1,
                                            result.endTime[2],
                                            result.endTime[3],
                                            result.endTime[4],
                                            result.endTime[5]);
                       me.jobProgress.problemInput.endTime = endDate;
                   }
                 }
               );

    let socket: any = new this.SockJS('/ws');
    this.stompClient = this.Stomp.over(socket);


    this.stompClient.connect({}, function(frame) {
      me.stompClient.subscribe('/topic/jobprogress.stream', message => me.processFunction(message));
    });

  }

}
