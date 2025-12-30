// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, ModuleWithProviders, NgModule} from "@angular/core"

@Component({
  template: `<div></div>`
})
export class Component3 {

}

@NgModule({
  declarations: [
    Component3
  ]
})
export class Module3 {

  static forRoot(): ModuleWithProviders<Module3> {
    return {
      ngModule: Module3,
      providers: []
    }
  }

}
