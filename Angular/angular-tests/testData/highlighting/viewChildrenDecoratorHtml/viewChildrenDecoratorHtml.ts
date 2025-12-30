// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, ElementRef, ViewChildren, ViewChild} from '@angular/core';

@Component({
  selector: 'app-test',
  templateUrl: './viewChildrenDecoratorHtml.html'
})
export class TestComponent {
  @ViewChild('area') area1!: ElementRef;
  @ViewChild('area3') area3!: ElementRef;

  @ViewChildren('area2') area2!: ElementRef;
  @ViewChildren('area4') area4!: ElementRef;
}
