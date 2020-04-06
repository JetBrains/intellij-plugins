// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, NgModule, Pipe} from "@angular/core";

@Component({})
class <error descr="Component1 is declared in multiple Angular modules: Module1 and Module2">Component1</error> {
}

@Component({})
class Component2 {
}

@Component({})
class <error descr="Component3 is not declared in any Angular module">Component3</error> {
}

@Component()
class <error descr="MyComponent is not declared in any Angular module">MyComponent</error> {
}

@Component()
abstract class MyComponentAbstract {
}

@Directive({})
class <error descr="Directive1 is declared in multiple Angular modules: Module1 and Module2">Directive1</error> {
}

@Directive({})
class Directive2 {
}

@Directive({})
class <error descr="Directive3 is not declared in any Angular module">Directive3</error> {
}

@Directive()
abstract class MyDirectiveAbstract {
}

@Directive()
class MyDirective {
}

@Pipe({})
class <error descr="Pipe1 is declared in multiple Angular modules: Module1 and Module2">Pipe1</error> {
}

@Pipe({})
class Pipe2 {
}

@Pipe({})
class <error descr="Pipe3 is not declared in any Angular module">Pipe3</error> {
}

@Pipe()
class <error descr="MyPipe is not declared in any Angular module">MyPipe</error> {
}

@Pipe()
abstract class MyPipeAbstract {
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
