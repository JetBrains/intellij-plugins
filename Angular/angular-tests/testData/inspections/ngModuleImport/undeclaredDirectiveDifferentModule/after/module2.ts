// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import {NgModule} from "@angular/core";
import {MyDirective} from "./directive";

@NgModule({
    exports: [
        MyDirective
    ],

    declarations: [
        MyDirective
    ]
})
export class Module2 {

}