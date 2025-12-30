// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, ElementRef, viewChildren, viewChild} from '@angular/core';

@Component({
  selector: 'app-test',
  templateUrl: './viewChildrenSignal.html'
})
export class TestComponent {
  area1 = viewChild<ElementRef>("area");
  area3 = viewChild<ElementRef>("area3");

  area1req = viewChild.required<ElementRef>("areaReq");

  area2 = viewChildren<ElementRef>("area2");
  area4 = viewChildren<ElementRef>("area4");

  badArea1 = viewChild<ElementRef>("badArea");
  badArea2 = viewChildren<ElementRef>("badArea");
}
