// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import {Component, NgModule} from "@angular/core";
import {NgFor} from "@angular/common";
import {FooBarDirective} from "./foo-bar.directive";

@Component({
   templateUrl: "./missing-required-directive-input-bindings.html"
 })
class TheComponent {

}

@NgModule({
    declarations: [
      FooBarDirective,
      TheComponent
    ],
    imports: [NgFor]
  })
export class MyModule {

}