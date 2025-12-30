// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";


@Component({
  selector: 'app-test',
  imports: [CommonModule, <error descr="TS2449: Class 'FooDirective1' used before its declaration.">FooDirective1</error>, <error descr="TS2449: Class 'FooDirective2' used before its declaration.">FooDirective2</error>],
  standalone: true,
  template: `
    <div *appFoo="personPromise | async as person">
      {{expectPerson(person)}}
      {{expectNumber(<error descr="TS2345: Argument of type 'Person' is not assignable to parameter of type 'number'.">person</error>)}}
    </div>
    <footer>{{<error descr="TS7053: Element implicitly has an 'any' type because expression of type '0' can't be used to index type 'Person'.
  Property '0' does not exist on type 'Person'."><error descr="TS2531: Object is possibly 'null'.">(personPromise | async)</error>[0]</error>}}</footer> <!-- ensure that null checks work -->
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
}

@Directive({
  selector: '[appFoo]',
  standalone: true
})
export class FooDirective2<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  /**
   * need to make sure that FooDirective1.appFoo doesn't widen our T to include undefined | null
   */
  @Input() appFoo: T | undefined | null;

  static ngTemplateContextGuard<T>(_dir: FooDirective2<T>, _ctx: unknown): _ctx is FooContext<T> {
    return true;
  }
}
