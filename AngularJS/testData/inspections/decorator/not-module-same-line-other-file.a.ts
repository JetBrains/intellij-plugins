// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {NgModule} from "@angular/core";
import {Component1} from "./not-module-same-line-other-file.b";

@NgModule({
    imports: [
        <error descr="Component Component1 is not standalone and cannot be imported directly. It must be imported via an NgModule.">Component1</error>
    ]
})
class Module1 {
}
