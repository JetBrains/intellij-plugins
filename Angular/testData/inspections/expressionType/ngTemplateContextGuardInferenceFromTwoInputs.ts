// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test-one',
  imports: [CommonModule, <error descr="TS2449: Class 'AgreeDirective' used before its declaration.">AgreeDirective</error>],
  standalone: true,
  template: `
    <div *appAgree="personPromise | async as person; second: personPromise | async">
      {{expectPerson(<error descr="TS2345: Argument of type 'Person | null' is not assignable to parameter of type 'Person'.
  Type 'null' is not assignable to type 'Person'.">person</error>)}}
      {{expectNumber(<error descr="TS2345: Argument of type 'Person | null' is not assignable to parameter of type 'number'.
  Type 'null' is not assignable to type 'number'.">person</error>)}}
    </div>
    <div *appAgree="null as person; second: undefined">
      {{expectPerson(<error descr="TS2345: Argument of type 'null' is not assignable to parameter of type 'Person'.">person</error>)}}
      {{expectNumber(<error descr="TS2345: Argument of type 'null' is not assignable to parameter of type 'number'.">person</error>)}}
    </div>
    <div *appAgree="null as person"> <!-- Angular types variables as any in case of omitted inputs -->
      {{expectPerson(person)}}
      {{expectNumber(person)}}
    </div>
    <div *appAgree="personPromise | async as person"> <!-- Angular types variables as any in case of omitted inputs -->
      {{expectPerson(person)}}
      {{expectNumber(person)}}
    </div>
    <footer>{{<error descr="TS7053: Element implicitly has an 'any' type because expression of type '0' can't be used to index type 'Person'.
  Property '0' does not exist on type 'Person'."><error descr="TS2531: Object is possibly 'null'.">(personPromise | async)</error>[0]</error>}}</footer> <!-- ensure that null checks work -->
  `,
})
export class TestComponentOne extends <error descr="TS2449: Class 'TestComponentBase' used before its declaration.">TestComponentBase</error> {
}

@Component({
  selector: 'app-test-two',
  imports: [CommonModule, <error descr="TS2449: Class 'AgreeDirective' used before its declaration.">AgreeDirective</error>, <error descr="TS2449: Class 'AgreeDirectiveDuplicate' used before its declaration.">AgreeDirectiveDuplicate</error>],
  standalone: true,
  template: `
    <div *appAgree="personPromise | async as person; second: personPromise | async">
      {{expectPerson(<error descr="TS2345: Argument of type 'Person | null' is not assignable to parameter of type 'Person'.
  Type 'null' is not assignable to type 'Person'.">person</error>)}}
      {{expectNumber(<error descr="TS2345: Argument of type 'Person | null' is not assignable to parameter of type 'number'.
  Type 'null' is not assignable to type 'number'.">person</error>)}}
    </div>
    <div *appAgree="null as person; second: undefined">
      {{expectPerson(<error descr="TS2345: Argument of type 'null' is not assignable to parameter of type 'Person'.">person</error>)}}
      {{expectNumber(<error descr="TS2345: Argument of type 'null' is not assignable to parameter of type 'number'.">person</error>)}}
    </div>
    <div *appAgree="null as person"> <!-- Angular types variables as any in case of omitted inputs -->
      {{expectPerson(person)}}
      {{expectNumber(person)}}
    </div>
    <div *appAgree="personPromise | async as person"> <!-- Angular types variables as any in case of omitted inputs -->
      {{expectPerson(person)}}
      {{expectNumber(person)}}
    </div>
    <footer>{{<error descr="TS7053: Element implicitly has an 'any' type because expression of type '0' can't be used to index type 'Person'.
  Property '0' does not exist on type 'Person'."><error descr="TS2531: Object is possibly 'null'.">(personPromise | async)</error>[0]</error>}}</footer> <!-- ensure that null checks work -->
  `,
})
export class TestComponentTwo extends <error descr="TS2449: Class 'TestComponentBase' used before its declaration.">TestComponentBase</error> {
}

abstract class TestComponentBase {
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

interface AgreeContext<T> {
  $implicit: T;
  appAgree: T;
}

@Directive({
  selector: '[appAgree]',
  standalone: true
})
export class AgreeDirective<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appAgree!: T;
  @Input() appAgreeSecond!: T | undefined | null;

  static ngTemplateContextGuard<T>(<weak_warning descr="TS6133: 'dir' is declared but its value is never read.">dir</weak_warning>: AgreeDirective<T>, <weak_warning descr="TS6133: 'ctx' is declared but its value is never read.">ctx</weak_warning>: unknown): ctx is AgreeContext<T> {
    return true;
  }
}

@Directive({
  selector: '[appAgree]',
  standalone: true
})
export class AgreeDirectiveDuplicate<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appAgree: T | undefined | null;
}
