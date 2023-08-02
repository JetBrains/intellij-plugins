// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Directive} from '@angular/core';
import {BoldDirective} from "./bold.directive";
import {UnderlineDirective} from "./underline.directive";

@Directive({
  selector: '[appMouseenter]',
  standalone: true,
  hostDirectives: [{
    directive: BoldDirective,
    outputs: ['hover']
  }, {
    directive: UnderlineDirective,
    inputs: ['color: underli<caret>neColor']
  }]
})
export class MouseenterDirective {
  constructor() {
  }
}
