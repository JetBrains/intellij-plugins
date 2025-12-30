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
        <error descr="Component ComponentNonStandalone is not standalone and cannot be imported directly. It must be imported via an NgModule.">ComponentNonStandalone</error>, //import a
        <error descr="Directive DirectiveNonStandalone is not standalone and cannot be imported directly. It must be imported via an NgModule.">DirectiveNonStandalone</error>, //import a
        <error descr="Pipe PipeNonStandalonePipe is not standalone and cannot be imported directly. It must be imported via an NgModule.">PipeNonStandalonePipe</error>, //import a
        <error descr="Class MyClass cannot be imported (neither an Angular module nor a standalone declarable)">MyClass</error>,
        <error descr="Class MyClass2 cannot be imported (neither an Angular module nor a standalone declarable)">MyClass2</error>,
    ],
    declarations: [
        <error descr="Component Component1 is standalone, and cannot be declared in an Angular module. Did you mean to import it instead?">Component1</error>,
        <error descr="Directive Directive1 is standalone, and cannot be declared in an Angular module. Did you mean to import it instead?">Directive1</error>,
        <error descr="Pipe Pipe1 is standalone, and cannot be declared in an Angular module. Did you mean to import it instead?">Pipe1</error>,
        ComponentNonStandalone,
        DirectiveNonStandalone,
        PipeNonStandalonePipe,
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
        <error descr="Component Component1 is standalone, and cannot be declared in an Angular module. Did you mean to import it instead?">Component1</error>, // move
        <error descr="Directive Directive1 is standalone, and cannot be declared in an Angular module. Did you mean to import it instead?">Directive1</error>, // move
        <error descr="Pipe Pipe1 is standalone, and cannot be declared in an Angular module. Did you mean to import it instead?">Pipe1</error>, // move
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
        <error descr="Class MyClass cannot be imported (neither an Angular module nor a standalone declarable)">MyClass</error>,
        <error descr="Class MyClass2 cannot be imported (neither an Angular module nor a standalone declarable)">MyClass2</error>,

        <error descr="Directive DirectiveNonStandalone is not standalone and cannot be imported directly. It must be imported via an NgModule.">DirectiveNonStandalone</error>, //import b
        <error descr="Pipe PipeNonStandalonePipe is not standalone and cannot be imported directly. It must be imported via an NgModule.">PipeNonStandalonePipe</error>, //import b
        <error descr="Component ComponentNonStandalone is not standalone and cannot be imported directly. It must be imported via an NgModule.">ComponentNonStandalone</error>, //import b
        Module1
    ]
})
class ComponentStandalone1 {
}