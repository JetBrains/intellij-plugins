// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Directive, NgModule} from '@angular/core';
import {RouterModule} from "./router/router_module";
import {RouterOutlet} from "./router/directives/router_outlet";
import {BrowserModule} from "./browser";
import {AsyncPipe, DatePipe} from "./pipes";

@Directive({
    selector: "[attr]"
})
export class MyDirective {

}

@NgModule({
    imports: [
       BrowserModule
    ],
    declarations: [
        MyDirective
    ],
    exports: [
        AsyncPipe,
        DatePipe
    ]
})
export class BrowserModuleTest {

}
