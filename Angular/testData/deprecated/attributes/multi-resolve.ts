// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input} from "@angular/core"

@Component({
  selector: "foo",
  template: ""
})
export class MyComp2 {

  @Input() bar: /*c2*/ number

  @Input() boo: /*c2*/ string

}

@Component({
  selector: "foo",
  templateUrl: "./multi-resolve.html"
})
export class MyComp {

  @Input() id: /*c1*/ string;

  @Input() bar: /*c1*/ number

  @Input() boo: /*c1*/ number
}
