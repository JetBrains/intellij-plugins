// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import {Component, Input} from "@angular/core"

@Component({
 selector: "foo",
 template: `
    <foo 
      [bar]="2" 
      [boo]="3"
    ></foo>
 `
})
export class MyComp2 {

  @Input() bar!: /*c2*/ number

  @Input() boo!: /*c2*/ string

  static ngAcceptInputType_bar: string | boolean;

  ngAcceptInputType_boo!: string | boolean;

}
