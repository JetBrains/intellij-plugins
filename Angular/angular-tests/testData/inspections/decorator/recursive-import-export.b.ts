// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.


import {NgModule, Component} from "@angular/core";
import {MY_IMPORTS} from "./recursive-import-export.a";

@Component({
     standalone: true,
     imports: MY_IMPORTS
})
export class MyComponent1 {

}

@NgModule({
    imports: [MyComponent1]
})
export class MyModule1 {

}