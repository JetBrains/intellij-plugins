// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";


@Component({
  selector: 'app-test',
  imports: [CommonModule, FooDirective1, FooDirective2],
  standalone: true,
  template: `
    <div *appFoo1="personPromise | async as local; let another = whatever">
      {{expectPerson(local)}}
      {{expectNumber(local)}}
      {{local.<weak_warning descr="Unresolved variable allYouEverWanted">allYouEverWanted</weak_warning>}}
      {{expectPerson(another)}}
      {{expectNumber(another)}}
      {{another.<weak_warning descr="Unresolved variable allYouEverWanted">allYouEverWanted</weak_warning>}}
      {{expectNumber(<error descr="Unresolved variable or type whatever">whatever</error>)}}
    </div>
    <div *appFoo2="personPromise | async as local; let another = whatever">
      {{expectPerson(local)}}
      {{expectNumber(local)}}
      {{local.<weak_warning descr="Unresolved variable allYouEverWanted">allYouEverWanted</weak_warning>}}
      {{expectPerson(another)}}
      {{expectNumber(another)}}
      {{another.<weak_warning descr="Unresolved variable allYouEverWanted">allYouEverWanted</weak_warning>}}
      {{expectNumber(<error descr="Unresolved variable or type whatever">whatever</error>)}}
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
}

@Directive({
  selector: '[appFoo1]',
  standalone: true
})
export class FooDirective1<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<FooContext<T>>) {
  }

  @Input() appFoo1!: T;
}


@Directive({
  selector: '[appFoo2]',
  standalone: true
})
export class FooDirective2<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appFoo2!: T;
}
