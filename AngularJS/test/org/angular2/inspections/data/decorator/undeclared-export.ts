// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, NgModule, Pipe} from "@angular/core";

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

@NgModule({
    imports: [
        Module3,
    ],
    declarations: [
        Component1,
        Directive1,
        Pipe1,
    ],
    exports: [
        Component1,
        <error descr="Cannot export Component2 from Module1 as it is neither declared nor imported in it">Component2</error>,
        Component3,
        Directive1,
        <error descr="Cannot export Directive2 from Module1 as it is neither declared nor imported in it">Directive2</error>,
        Directive3,
        Pipe1,
        <error descr="Cannot export Pipe2 from Module1 as it is neither declared nor imported in it">Pipe2</error>,
        Pipe3,
    ]
})
class Module1 {
}

@NgModule({
    declarations: [
        Component3,
        Pipe3,
    ],
    exports: [
        Component3,
        <error descr="Cannot export Directive3 from Module2 as it is neither declared nor imported in it">Directive3</error>,
        Pipe3,
    ]
})
class Module2 {
}

@NgModule({
    declarations: [
        Component2,
        Directive2,
        Pipe2,
    ],
    exports: [
        Module2
    ]
})
class Module3 {
}

@NgModule({
    declarations: [
        Foo
    ],
    exports: [
        <weak_warning descr="Cannot export Component2 from Module4 as it is neither declared nor imported in it">Component2</weak_warning>
    ]
})
class Module4 {
}
