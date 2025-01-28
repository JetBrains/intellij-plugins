// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, ElementRef, viewChildren, viewChild} from '@angular/core';

@Component({
  selector: 'app-test',
  template: `
    <!-- ViewChild should grab only first reference from each template block -->
    <textarea #area></textarea>
    <textarea #area2></textarea>

    @if (true) {
      <div #area3></div>
    } @else {
      <div #area3></div>
    }
    <div *ngIf="true">
      <div #area3></div>
    </div>
    <div *ngIf="true">
      <div #area3></div>
    </div>
  `
})
export class TestComponent {
  area1 = viewChild("area1");
  area2 = viewChild<ElementRef>("area2");

  area3 = viewChild.required("area3");
  area4 = viewChild.required<ElementRef>("area4");

  area5 = viewChildren("area5");
  area6 = viewChildren<ElementRef>("area6");

}
