// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test',
  imports: [CommonModule, <error descr="TS2449: Class 'HelloDirective' used before its declaration.">HelloDirective</error>, <error descr="TS2449: Class 'HelloIgnoredGenericDirective' used before its declaration.">HelloIgnoredGenericDirective</error>, <error descr="TS2449: Class 'HelloInternalGenericDirective' used before its declaration.">HelloInternalGenericDirective</error>],
  standalone: true,
  template: `
    <div *appHello="let local">
      {{local.toFixed()}}
      {{expectNumber(local)}}
      {{expectPerson(<error descr="TS2345: Argument of type 'number' is not assignable to parameter of type 'Person'.">local</error>)}}
      {{local.<error descr="TS2339: Property 'leftovers' does not exist on type 'number'.">leftovers</error>}}
    </div>
    <div *appHelloIgnoredGeneric="(personPromise | async)!; let local">
      {{local.toFixed()}}
      {{expectNumber(local)}}
      {{expectPerson(<error descr="TS2345: Argument of type 'number' is not assignable to parameter of type 'Person'.">local</error>)}}
      {{local.<error descr="TS2339: Property 'leftovers' does not exist on type 'number'.">leftovers</error>}}
    </div>
    <div *appHelloInternalGeneric="let local">
      {{local.toLowerCase()}}
      {{expectNumber(<error descr="TS2345: Argument of type 'string' is not assignable to parameter of type 'number'.
  Type 'string' is not assignable to type 'number'.">local</error>)}}
    </div>
  `,
})
export class TestComponent {
  personPromise = Promise.resolve<Person>({
    familyName: 'Doe'
  });

  expectPerson(num: Person): Person {
    return num;
  }

  expectNumber(num: number): number {
    return num;
  }
}

export interface Person {
  familyName: string;
}

export interface Misdirection {
  ignoreMe: string;
  $implicit: { leftovers: number, hungry: boolean };
}

export interface SimpleContext {
  $implicit: number;
}

@Directive({
  selector: '[appHello]',
  standalone: true
})
export class HelloDirective {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<Misdirection>) {}

  static ngTemplateContextGuard(_dir: HelloDirective, _ctx: unknown): _ctx is SimpleContext {
    return true;
  }
}

@Directive({
  selector: '[appHelloIgnoredGeneric]',
  standalone: true
})
export class HelloIgnoredGenericDirective<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<Misdirection>) {}

  @Input() appHelloIgnoredGeneric!: T

  static ngTemplateContextGuard<Type>(_dir: HelloIgnoredGenericDirective<Type>, _ctx: unknown): _ctx is SimpleContext {
    return true;
  }
}

export interface ImplicitContext<T> {
  $implicit: T;
}

@Directive({
  selector: '[appHelloInternalGeneric]',
  standalone: true
})
export class HelloInternalGenericDirective {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<ImplicitContext<string>>) {}

  static ngTemplateContextGuard(_dir: HelloInternalGenericDirective, _ctx: unknown): _ctx is ImplicitContext<'left' | 'right'> {
    return true;
  }
}
