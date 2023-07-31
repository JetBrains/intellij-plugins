// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Directive, Output, EventEmitter, Input} from '@angular/core';

@Directive({
   selector: '[appBold]',
   outputs: ["<caret>"],
 })
export class BoldDirective {
  field1 = new EventEmitter<String>()
  field2  = new EventEmitter<String>()

  @Output("alias3")
  field3 = new EventEmitter<Number>()

  @Input()
  field4 = new EventEmitter<String>()
}
