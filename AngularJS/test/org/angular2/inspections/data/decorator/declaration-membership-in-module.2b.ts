// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, NgModule, Pipe} from "@angular/core";

import {Component1 as c1} from "./declaration-membership-in-module.2a";

@Component({})
class Component1 {
}

@NgModule({
    declarations: [
        Component1,
        Component2,
    ]
})
class Module1 {
}

@NgModule({
    imports: [
        Module1
    ],
    declarations: [
        c1,
    ],
    exports: [
        Component2,
    ]
})
class Module2 {
}