// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import {Component, NgModule} from "@angular/core"
import {MyLibModule} from "my-lib"

@Component({
  templateUrl: "./template.html"
})
export class MyComponent {

  foo(test: string): boolean {
    return false;
  }

  bar(test: number): boolean {
    return false;
  }

  setEvent(ev: string) {

  }

  setEvent2(ev: boolean) {

  }

}

@NgModule({
  declarations: [
    MyComponent
  ],
  imports: [
    MyLibModule
  ]
})
export class MyModule {

}
