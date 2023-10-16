// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test',
  imports: [CommonModule, WeirdIfFromDirective],
  standalone: true,
  template: `
    <div *weirdIf="let local from personPromise | async also 5 as other">
      {{expectPerson(local)}} <-- todo Argument type unknown is not assignable to parameter type Person -->
      {{local.length}} <!-- todo Unresolved variable length -->
      {{local.<weak_warning descr="Unresolved variable familyName">familyName</weak_warning>}}
      {{local.<weak_warning descr="Unresolved variable accomplishments">accomplishments</weak_warning>}}
      {{expectPerson(<error descr="Argument type number is not assignable to parameter type Person">other</error>)}}
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

type ExcludeFalsy<T> = Exclude<T, false | 0 | '' | null | undefined>

// noinspection JSUnusedGlobalSymbols
export class WeirdIfFromContext<First = unknown, Second = unknown> {
  $implicit!: First;
  weirdIfFrom!: First;
  weirdIfAlso!: Second;
}

@Directive({
  selector: '[weirdIf][weirdIfFrom]',
  standalone: true,
})
export class WeirdIfFromDirective<One = unknown, Two = unknown> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<WeirdIfFromContext<One, Two>>) {}

  @Input() set weirdIfFrom(_condition: One) {}
  @Input() set weirdIfAlso(_condition: Two) {}

  static ngTemplateGuard_fancyIfFrom: 'binding';
  static ngTemplateGuard_fancyIfAlso: 'binding';

  // different type parameters names and order
  static ngTemplateContextGuard<Dos>(dir: WeirdIfFromDirective<unknown, Dos>, ctx: any): ctx is WeirdIfFromContext<unknown, ExcludeFalsy<Dos>> {
    return true;
  }
}
