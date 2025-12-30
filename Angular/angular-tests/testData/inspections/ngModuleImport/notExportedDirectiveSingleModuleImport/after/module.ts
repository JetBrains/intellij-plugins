// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.


import {NgModule} from "@angular/core";
import {MyComponent} from "./component";
import {Module2} from "./module2";

@NgModule({
    imports: [
        Module2
    ],
    declarations: [
        MyComponent
    ]
})
export class MyModule {

}