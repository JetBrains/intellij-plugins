// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, NgModule, Pipe} from "@angular/core";

@Component({})
export class Component1 {
}

@Component({})
export class <weak_warning descr="Component2 is not declared in any Angular module">Component2</weak_warning> {
}
