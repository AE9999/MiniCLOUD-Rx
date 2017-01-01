/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { JobprogressComponent } from './jobprogress.component';

describe('JobprogressComponent', () => {
  let component: JobprogressComponent;
  let fixture: ComponentFixture<JobprogressComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JobprogressComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JobprogressComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
