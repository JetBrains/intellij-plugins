// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, ModuleWithProviders, NgModule} from "@angular/core"

@Component({
  template: `<div></div>`
})
export class Component1 {

}

@NgModule({
  declarations: [
    Component1
  ]
})
export class Module1 {

  static forRoot(): ModuleWithProviders {
    return {
      ngModule: Module1,
      providers: []
    }
  }

}
