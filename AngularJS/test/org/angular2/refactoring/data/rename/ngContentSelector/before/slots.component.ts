// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core";

@Component({
  selector: "slots-component",
  template: `
      <ng-content select="foo, tag<caret>-slot, bar"></ng-content>
      <ng-content select="[attr-slot]"></ng-content>
  `
})
export class SlotsComponent {

  constructor() {
  }

}
