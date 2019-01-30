// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.


import {Component, NgModule} from "@angular/core";
import {MyComponent, MyDirective} from "./component";

@Component({
    templateUrl: "./template-reference-variable-with-module.html"
})
class RefVarComp {

}

@NgModule({
    declarations: [
        MyDirective,
        MyComponent,
        RefVarComp
    ]
})
export class MyModule {

}