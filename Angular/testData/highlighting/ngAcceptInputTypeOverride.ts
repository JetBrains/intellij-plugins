// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import {Component, Input} from "@angular/core"

@Component({
 selector: "foo",
 template: `
    <foo 
      <error descr="TS2322: Type '2' is not assignable to type 'string | boolean'.">[bar]</error>="2"  
      <error descr="TS2322: Type 'number' is not assignable to type 'string'.">[boo]</error>="3"
    ></foo>
 `
})
export class MyComp2 {

  @Input() bar!: /*c2*/ number

  @Input() boo!: /*c2*/ string

  static ngAcceptInputType_bar: string | boolean;

  <warning descr="Unused field ngAcceptInputType_boo">ngAcceptInputType_boo</warning>!: string | boolean;

}
