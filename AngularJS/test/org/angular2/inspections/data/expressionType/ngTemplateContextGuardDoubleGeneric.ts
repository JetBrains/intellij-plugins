// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test',
  imports: [CommonModule, FancyIfFromDirective],
  standalone: true,
  template: `
    <div *fancyIf="let person from personPromise | async also 5 as other">
      {{expectPerson(person)}}
      {{expectNumber(<error descr="Argument type  Person  is not assignable to parameter type  number ">person</error>)}}
      {{person.familyName}}
      {{person.<error descr="Unresolved variable accomplishments">accomplishments</error>}}
      {{expectPerson(<error descr="Argument type  number  is not assignable to parameter type  Person ">other</error>)}}
      {{expectNumber(other)}}
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

// noinspection JSUnusedGlobalSymbols
export class FancyIfFromContext<First = unknown, Second = unknown> {
  $implicit!: First;
  fancyIfFrom!: First;
  fancyIfAlso!: Second;
}

type ExcludeFalsy<T> = Exclude<T, false | 0 | '' | null | undefined>

@Directive({
  selector: '[fancyIf][fancyIfFrom]',
  standalone: true,
})
export class FancyIfFromDirective<One = unknown, Two = unknown> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<FancyIfFromContext<One, Two>>) {}

  @Input() set fancyIfFrom(_condition: One) {}
  @Input() set fancyIfAlso(_condition: Two) {}

  static ngTemplateGuard_fancyIfFrom: 'binding';
  static ngTemplateGuard_fancyIfAlso: 'binding';

  // different type parameters names and order
  static ngTemplateContextGuard<Dos, Uno>(dir: FancyIfFromDirective<Uno, Dos>, ctx: any): ctx is FancyIfFromContext<ExcludeFalsy<Uno>, ExcludeFalsy<Dos>> {
    return true;
  }
}