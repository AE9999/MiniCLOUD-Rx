import { Component, OnInit } from '@angular/core';
import {FileUploader} from "ng2-file-upload";


@Component({
  selector: 'app-cnf-submitter',
  templateUrl: './cnf-submitter.component.html',
  styleUrls: ['./cnf-submitter.component.css']
})
export class CnfSubmitterComponent implements OnInit {

  private URL: string = '/api/submit';

  public uploader:FileUploader = new FileUploader(<any> {url: URL});

  public hasBaseDropZoneOver:boolean = false;

  public hasAnotherDropZoneOver:boolean = false;

  constructor() { }

  public fileOverBase(e:any):void {
    this.hasBaseDropZoneOver = e;
  }

  public fileOverAnother(e:any):void {
    this.hasAnotherDropZoneOver = e;
  }

  ngOnInit() {
  }

}
