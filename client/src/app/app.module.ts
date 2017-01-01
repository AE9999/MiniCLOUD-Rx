import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import { JobprogressComponent } from './jobprogress/jobprogress.component';
import { CnfSubmitterComponent } from './cnf-submitter/cnf-submitter.component';
import {RouterModule} from "@angular/router";
import {FileUploadModule} from "ng2-file-upload";
import {Ng2BootstrapModule} from "ng2-bootstrap";

@NgModule({
  declarations: [
    AppComponent,
    JobprogressComponent,
    CnfSubmitterComponent
  ],
  imports: [
    FileUploadModule,
    Ng2BootstrapModule,
    BrowserModule,
    FormsModule,
    HttpModule,
    RouterModule.forRoot([
      { path: '', component: JobprogressComponent },
      // { path: 'upload', component: CnfSubmitterComponent },
      { path: 'progress', component: JobprogressComponent }
    ], { useHash: true }),

  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
