// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {NgModule} from "@angular/core";
import {Component1} from "./not-module-same-line-other-file.b";

@NgModule({
    imports: [
        <error descr="Class Component1 cannot be imported (neither an Angular module nor a standalone declarable)">Component1</error>
    ]
})
class Module1 {
}
