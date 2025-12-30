// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, Input, NgModule, Pipe} from "@angular/core";

@Component({standalone:false})
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

@Directive({standalone: true})
class DirectiveStandalone {
}

@Pipe({ standalone: true })
class PipeStandalone {
}

@Component({ standalone: true })
class ComponentStandalone {
}

@NgModule({
    imports: [
        Component1, //import a
        Directive1, //import a
        Pipe1, //import a
        Module2,
        ComponentStandalone,
        DirectiveStandalone,
        PipeStandalone,
        MyClass,
        MyClass2,
    ],
    declarations: [
        Component1,
        Directive1,
        Pipe1,
        ComponentStandalone,
        DirectiveStandalone,
        PipeStandalone,
        Module2,
        MyClass,
        MyClass2,
    ],
    exports: [
        Component1,
        Directive1,
        Pipe1,
        Module2,
        MyClass,
        MyClass2,
    ]
})
class Module1 {
}

@NgModule({
    imports: [
        Module1,
        PipeStandalone
    ],
    declarations: [
        Component1,
        Directive1,
        Pipe1,
        ComponentStandalone, // move
        DirectiveStandalone, // move
        // move
    ],
    exports: [
        Component2,
        Directive2,
        Pipe2
    ]
})
class Module2 {
}

@Component({
    standalone: true,
    imports: [
        Pipe1, //import b
        Component1, //import b
        Directive1, //import b
        MyClass,
        MyClass2,

        DirectiveStandalone,
        PipeStandalone,
        ComponentStandalone,
        Module1
    ]
})
class ComponentStandalone1 {
}