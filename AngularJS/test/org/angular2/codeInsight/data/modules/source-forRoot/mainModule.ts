// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import {NgModule} from "@angular/core"
import {Module1} from "./module1"
import {Module2} from "./module2"
import {Module3} from "./module3"

@NgModule({
  imports: [
    Module1.forRoot(),
    Module2.forRoot(),
    Module3.forRoot()
  ]
})
export class MainModule {

}
