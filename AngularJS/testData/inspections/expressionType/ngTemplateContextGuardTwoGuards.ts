// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";


@Component({
  selector: 'app-test',
  imports: [CommonModule, FooDirective1, FooDirective2],
  standalone: true,
  template: `
    <div *appFoo="personPromise | async as person">
      {{expectPerson(<error descr="Argument type  Person | null  is not assignable to parameter type  Person   Type  null  is not assignable to type  Person ">person</error>)}} <!-- should be no error -->
      {{expectNumber(<error descr="Argument type  Person | null  is not assignable to parameter type  number   Type  Person  is not assignable to type  number ">person</error>)}}
    </div>
    <footer>{{<error descr="Indexed expression can be null or undefined">(personPromise | async)</error>[0]}}</footer> <!-- ensure that null checks work -->
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

interface FooContext<T> {
  $implicit: T;
  appFoo: T;
}

@Directive({
  selector: '[appFoo]',
  standalone: true
})
export class FooDirective1<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appFoo!: T;

  static ngTemplateContextGuard<T>(dir: FooDirective1<T>, ctx: unknown): ctx is FooContext<T> {
    return true;
  }
}

@Directive({
  selector: '[appFoo]',
  standalone: true
})
export class FooDirective2<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appFoo: T | undefined | null;

  static ngTemplateContextGuard<T>(dir: FooDirective2<T>, ctx: unknown): ctx is FooContext<T> {
    return true;
  }
}
