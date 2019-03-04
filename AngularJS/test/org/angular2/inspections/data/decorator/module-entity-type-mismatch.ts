// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, Input, NgModule, Pipe} from "@angular/core";

@Component({})
class Component1 {
}

@Component({})
class Component2 {
}

@Component({})
class Component3 {
}

@Directive({})
class Directive1 {
}

@Directive({})
class Directive2 {
}

@Directive({})
class Directive3 {
}

@Pipe({})
class Pipe1 {
}

@Pipe({})
class Pipe2 {
}

@Pipe({})
class Pipe3 {
}

class MyClass {

}

@Input({})
class MyClass2 {

}

@NgModule({
    imports: [
        <error descr="Class Component1 is not an Angular module">Component1</error>,
        <error descr="Class Directive1 is not an Angular module">Directive1</error>,
        <error descr="Class Pipe1 is not an Angular module">Pipe1</error>,
        Module2,
        <error descr="Class MyClass is not an Angular module">MyClass</error>,
        <error descr="Class MyClass2 is not an Angular module">MyClass2</error>,
    ],
    declarations: [
        Component1,
        Directive1,
        Pipe1,
        <error descr="Class Module2 is neither an Angular component nor directive nor pipe">Module2</error>,
        <error descr="Class MyClass is neither an Angular component nor directive nor pipe">MyClass</error>,
        <error descr="Class MyClass2 is neither an Angular component nor directive nor pipe">MyClass2</error>,
    ],
    exports: [
        Component1,
        Directive1,
        Pipe1,
        Module2,
        <weak_warning descr="Class MyClass is neither an Angular module nor component nor directive nor pipe">MyClass</weak_warning>,
        <weak_warning descr="Class MyClass2 is neither an Angular module nor component nor directive nor pipe">MyClass2</weak_warning>,
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