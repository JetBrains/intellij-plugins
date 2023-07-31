

// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Directive, Input} from '@angular/core';

@Directive({
   selector: '[appBold]',
   standalone: true,
   inputs: ["field1: alias1", {name: "field2", alias: "alias2", required: false}, "virtual"],
 })
export class BoldDirective {
  title = "dd"

  field1 = "12"
  field2 = "12"
  field3 = "12"

  @Input({alias: "alias4", required: false})
  field4 = 12

  @Input("alias5")
  field5 = 12

  @Input()
  field6 = '12'
}

@Directive({
   selector: '[appMouseenter]',
   standalone: true,
   hostDirectives: [{
     directive: BoldDirective,
     inputs: [<caret>, 'foobar']
   }]
 })
export class MouseenterDirective {
}
