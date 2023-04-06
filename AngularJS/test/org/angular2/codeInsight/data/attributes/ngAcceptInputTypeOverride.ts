// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import {Component, Input} from "@angular/core"

@Component({
 selector: "foo",
 template: `
    <foo 
      [bar]="<error descr="Type 2 is not assignable to type string | boolean  Type 2 is not assignable to type boolean">2</error>" 
      [boo]="<error descr="Type 3 is not assignable to type string">3</error>"
    ></foo>
 `
})
export class MyComp2 {

  @Input() bar: /*c2*/ number

  @Input() boo: /*c2*/ string

  static ngAcceptInputType_bar: string | boolean;

  <warning descr="Unused field ngAcceptInputType_boo">ngAcceptInputType_boo</warning>: string | boolean;

}
