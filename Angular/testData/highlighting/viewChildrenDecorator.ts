// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, ElementRef, ViewChildren, ViewChild} from '@angular/core';

@Component({
  selector: 'app-test',
  template: `
      <!-- ViewChild should grab only first reference from each template block -->
      <textarea #area></textarea>
      <textarea #<warning descr="Unused constant area">area</warning>></textarea>
      
      @if(true) {
        <div #area3></div>
        <div #<warning descr="Unused constant area3">area3</warning>></div>
      } @else {
        <div #area3></div>
        <div #<warning descr="Unused constant area3">area3</warning>></div>
      }
      <div *ngIf="true"> <div #area3></div> <div #<warning descr="Unused constant area3">area3</warning>></div> </div>
      <div *ngIf="true"> <div #area3></div> <div #<warning descr="Unused constant area3">area3</warning>></div> </div>
      
      <!-- ViewChildren should grab all references within template blocks -->
      <div #area2></div>
      <div #area2></div>
      
      <div *ngIf="true"> <div #area4></div> <div #area4></div> </div>
      <div *ngIf="true"> <div #area4></div> <div #area4></div> </div>
    `
})
export class TestComponent {
  @ViewChild('area') area1!: ElementRef;
  @ViewChild('area3') area3!: ElementRef;

  @ViewChildren('area2') area2!: ElementRef;
  @ViewChildren('area4') area4!: ElementRef;

  @ViewChild('<warning descr="Unrecognized name">badArea</warning>') badArea1!: ElementRef;
  @ViewChildren('<warning descr="Unrecognized name">badArea</warning>') badArea2!: ElementRef;
}
