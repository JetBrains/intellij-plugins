// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.


import {Attribute, Component, Directive, Input} from "@angular/core"

@Component({
  template: `<div <caret>`
})
export class Comp {

}

@Directive({
  selector: "[foo]"
})
export class Dir {
  constructor(@Attribute("foo") foo, @Attribute("bar") bar) {
  }

  @Input("test")
  private foo: boolean;
}
