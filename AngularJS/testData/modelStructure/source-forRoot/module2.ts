// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, NgModule} from "@angular/core"

@Component({
  template: `<div></div>`
})
export class Component2 {

}

@NgModule({
  declarations: [
    Component2
  ]
})
export class Module2 {

  static forRoot() {
    return {
      ngModule: Module2,
      providers: []
    }
  }

}
