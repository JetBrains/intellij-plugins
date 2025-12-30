// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, Input, NgModule, Pipe} from "@angular/core";

@Component({standalone:true})
class Component1 {
}

@Component({})
class Component2 {
}

@Component({})
class Component3 {
}

@Directive({
    standalone: false
})
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

@Directive({standalone: false})
class DirectiveNonStandalone {
}

@Pipe({ standalone: false })
class PipeNonStandalonePipe {
}

@Component({ standalone: false })
class ComponentNonStandalone {
}

@NgModule({
    imports: [
        Component1,
        Directive1,
        Pipe1,
        Module2,
        ComponentNonStandalone, //import a
        DirectiveNonStandalone, //import a
        PipeNonStandalonePipe, //import a
        MyClass,
        MyClass2,
    ],
    declarations: [
        Component1,
        Directive1,
        Pipe1,
        ComponentNonStandalone,
        DirectiveNonStandalone,
        PipeNonStandalonePipe,
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
        Module1
    ],
    declarations: [
        Component1, // move
        Directive1, // move
        Pipe1, // move
        ComponentNonStandalone,
        DirectiveNonStandalone,
        PipeNonStandalonePipe,
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
    imports: [
        Pipe1,
        Component1,
        Directive1,
        MyClass,
        MyClass2,

        DirectiveNonStandalone, //import b
        PipeNonStandalonePipe, //import b
        ComponentNonStandalone, //import b
        Module1
    ]
})
class ComponentStandalone1 {
}