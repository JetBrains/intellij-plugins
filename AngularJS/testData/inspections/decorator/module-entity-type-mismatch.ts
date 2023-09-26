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
        <error descr="Component Component1 is not standalone and cannot be imported directly. It must be imported via an NgModule." textAttributesKey="ERRORS_ATTRIBUTES">Component1</error>, //import a
        <error descr="Directive Directive1 is not standalone and cannot be imported directly. It must be imported via an NgModule." textAttributesKey="ERRORS_ATTRIBUTES">Directive1</error>, //import a
        <error descr="Pipe Pipe1 is not standalone and cannot be imported directly. It must be imported via an NgModule." textAttributesKey="ERRORS_ATTRIBUTES">Pipe1</error>, //import a
        Module2,
        ComponentStandalone,
        DirectiveStandalone,
        PipeStandalone,
        <error descr="Class MyClass cannot be imported (neither an Angular module nor a standalone declarable)" textAttributesKey="ERRORS_ATTRIBUTES">MyClass</error>,
        <error descr="Class MyClass2 cannot be imported (neither an Angular module nor a standalone declarable)" textAttributesKey="ERRORS_ATTRIBUTES">MyClass2</error>,
    ],
    declarations: [
        Component1,
        Directive1,
        Pipe1,
        <error descr="Component ComponentStandalone is standalone, and cannot be declared in an Angular module. Did you mean to import it instead?" textAttributesKey="ERRORS_ATTRIBUTES">ComponentStandalone</error>,
        <error descr="Directive DirectiveStandalone is standalone, and cannot be declared in an Angular module. Did you mean to import it instead?" textAttributesKey="ERRORS_ATTRIBUTES">DirectiveStandalone</error>,
        <error descr="Pipe PipeStandalone is standalone, and cannot be declared in an Angular module. Did you mean to import it instead?" textAttributesKey="ERRORS_ATTRIBUTES">PipeStandalone</error>,
        <error descr="Class Module2 is neither an Angular component nor directive nor pipe" textAttributesKey="ERRORS_ATTRIBUTES">Module2</error>,
        <error descr="Class MyClass is neither an Angular component nor directive nor pipe" textAttributesKey="ERRORS_ATTRIBUTES">MyClass</error>,
        <error descr="Class MyClass2 is neither an Angular component nor directive nor pipe" textAttributesKey="ERRORS_ATTRIBUTES">MyClass2</error>,
    ],
    exports: [
        Component1,
        Directive1,
        Pipe1,
        Module2,
        <weak_warning descr="Class MyClass is neither an Angular module nor component nor directive nor pipe" textAttributesKey="INFO_ATTRIBUTES">MyClass</weak_warning>,
        <weak_warning descr="Class MyClass2 is neither an Angular module nor component nor directive nor pipe" textAttributesKey="INFO_ATTRIBUTES">MyClass2</weak_warning>,
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

@Component({
    standalone: true,
    imports: [
        <error descr="Pipe Pipe1 is not standalone and cannot be imported directly. It must be imported via an NgModule." textAttributesKey="ERRORS_ATTRIBUTES">Pipe1</error>, //import b
        <error descr="Component Component1 is not standalone and cannot be imported directly. It must be imported via an NgModule." textAttributesKey="ERRORS_ATTRIBUTES">Component1</error>, //import b
        <error descr="Directive Directive1 is not standalone and cannot be imported directly. It must be imported via an NgModule." textAttributesKey="ERRORS_ATTRIBUTES">Directive1</error>, //import b
        <error descr="Class MyClass cannot be imported (neither an Angular module nor a standalone declarable)" textAttributesKey="ERRORS_ATTRIBUTES">MyClass</error>,
        <error descr="Class MyClass2 cannot be imported (neither an Angular module nor a standalone declarable)" textAttributesKey="ERRORS_ATTRIBUTES">MyClass2</error>,

        DirectiveStandalone,
        PipeStandalone,
        ComponentStandalone,
        Module1
    ]
})
class ComponentStandalone1 {
}