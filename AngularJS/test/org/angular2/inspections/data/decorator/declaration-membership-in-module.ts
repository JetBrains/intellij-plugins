// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, NgModule, Pipe} from "@angular/core";

@Component({})
class <error descr="Declaration is included in more than one NgModule.">Component1</error> {
}

@Component({})
class Component2 {
}

@Component({})
class <error descr="Declaration is not included in any NgModule.">Component3</error> {
}

@Directive({})
class <error descr="Declaration is included in more than one NgModule.">Directive1</error> {
}

@Directive({})
class Directive2 {
}

@Directive({})
class <error descr="Declaration is not included in any NgModule.">Directive3</error> {
}

@Pipe({})
class <error descr="Declaration is included in more than one NgModule.">Pipe1</error> {
}

@Pipe({})
class Pipe2 {
}

@Pipe({})
class <error descr="Declaration is not included in any NgModule.">Pipe3</error> {
}

@NgModule({
    declarations: [
        Component1,
        Component2,
        Directive1,
        Directive2,
        Pipe1,
        Pipe2
    ]
})
class Module1 {
}

@NgModule({
    imports: [
        Module1
    ],
    declarations: [
        Component1,
        Directive1,
        Pipe1,
    ],
    exports: [
        Component2,
        Directive2,
        Pipe2
    ]
})
class Module2 {
}