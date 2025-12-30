// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, ElementRef, ViewChildren, ViewChild} from '@angular/core';

@Component({
    selector: 'app-test',
    template: `
      <textarea #area></textarea>
      <div #area2></div>
      <div #area2></div>
      <div #area2></div>
      <div *ngIf="true" #area3></div>
      <div *ngIf="true" #area3></div>
    `
})
export class TestComponent {
    @ViewChildren('area') area: ElementRef;

    @ViewChild('area') area2: ElementRef;

}
