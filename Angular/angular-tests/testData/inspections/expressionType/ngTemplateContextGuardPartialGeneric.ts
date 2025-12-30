// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test',
  imports: [CommonModule, <error descr="TS2449: Class 'WeirdIfFromDirective' used before its declaration.">WeirdIfFromDirective</error>],
  standalone: true,
  template: `
    <div *weirdIf="let local from 12 also personPromise | async as other">
      {{expectPerson(<error descr="TS2345: Argument of type 'unknown' is not assignable to parameter of type 'Person'.">local</error>)}}
      {{other.<error descr="TS2339: Property 'length' does not exist on type 'Person'.">length</error>}} <!-- todo Unresolved variable length -->
      {{other.familyName}}
      {{other.<error descr="TS2339: Property 'accomplishments' does not exist on type 'Person'.">accomplishments</error>}}
      {{expectPerson(other)}}
      {{expectNumber(<error descr="TS2345: Argument of type 'Person' is not assignable to parameter of type 'number'.">other</error>)}}
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
  static ngTemplateContextGuard<Dos>(_dir: WeirdIfFromDirective<unknown, Dos>, _ctx: any): _ctx is WeirdIfFromContext<unknown, ExcludeFalsy<Dos>> {
    return true;
  }
}
