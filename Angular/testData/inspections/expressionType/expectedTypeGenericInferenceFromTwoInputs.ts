// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, EventEmitter, Input, Output} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test-one',
  imports: [CommonModule, <error descr="TS2449: Class 'AgreeDirective' used before its declaration." textAttributesKey="ERRORS_ATTRIBUTES">AgreeDirective</error>],
  standalone: true,
  template: `
    <span [appAgree]="null" [appAgreeSecond]="simpleField"></span> <!-- todo no errors, bug in TS -->
    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField"></span>
    <span [(appAgree)]="simpleField" [appAgreeSecond]="simpleField"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async"></span>

    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField" (appAgreeChange)="expectNumber(<error descr="TS2345: Argument of type 'number | null' is not assignable to parameter of type 'number'.
  Type 'null' is not assignable to type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)"></span>
    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField" (appAgreeChange)="expectNumber($event!)"></span>
    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField" (appAgreeChange)="expectPerson(<error descr="TS2345: Argument of type 'number' is not assignable to parameter of type 'Person'." textAttributesKey="ERRORS_ATTRIBUTES">$event!</error>)"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async" (appAgreeChange)="expectPerson(<error descr="TS2345: Argument of type 'Person | null' is not assignable to parameter of type 'Person'.
  Type 'null' is not assignable to type 'Person'." textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async" (appAgreeChange)="expectPerson($event!)"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async" (appAgreeChange)="expectNumber(<error descr="TS2345: Argument of type 'Person' is not assignable to parameter of type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">$event!</error>)"></span>

    <footer>{{<error descr="TS7053: Element implicitly has an 'any' type because expression of type '0' can't be used to index type 'Person'.
  Property '0' does not exist on type 'Person'." textAttributesKey="ERRORS_ATTRIBUTES"><error descr="TS2531: Object is possibly 'null'." textAttributesKey="ERRORS_ATTRIBUTES">(personPromise | async)</error>[0]</error>}}</footer> <!-- ensure that null checks work -->
  `,
})
export class TestComponentOne extends <error descr="TS2449: Class 'TestComponentBase' used before its declaration." textAttributesKey="ERRORS_ATTRIBUTES">TestComponentBase</error> {
}

// including AgreeDirectiveDuplicate previously produced still wrong errors, though slightly differently wrong
@Component({
  selector: 'app-test-two',
  imports: [CommonModule, <error descr="TS2449: Class 'AgreeDirective' used before its declaration." textAttributesKey="ERRORS_ATTRIBUTES">AgreeDirective</error>, <error descr="TS2449: Class 'AgreeDirectiveDuplicate' used before its declaration." textAttributesKey="ERRORS_ATTRIBUTES">AgreeDirectiveDuplicate</error>],
  standalone: true,
  template: `
    <span [appAgree]="null" [appAgreeSecond]="simpleField"></span>
    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField"></span>
    <span [(appAgree)]="simpleField" [appAgreeSecond]="simpleField"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async"></span>

    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField" (appAgreeChange)="expectNumber(<error descr="TS2345: Argument of type 'number | null' is not assignable to parameter of type 'number'.
  Type 'null' is not assignable to type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)"></span>
    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField" (appAgreeChange)="expectNumber($event!)"></span>
    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField" (appAgreeChange)="expectPerson(<error descr="TS2345: Argument of type 'number' is not assignable to parameter of type 'Person'." textAttributesKey="ERRORS_ATTRIBUTES">$event!</error>)"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async" (appAgreeChange)="expectPerson(<error descr="TS2345: Argument of type 'Person | null' is not assignable to parameter of type 'Person'.
  Type 'null' is not assignable to type 'Person'." textAttributesKey="ERRORS_ATTRIBUTES">$event</error>)"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async" (appAgreeChange)="expectPerson($event!)"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async" (appAgreeChange)="expectNumber(<error descr="TS2345: Argument of type 'Person' is not assignable to parameter of type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">$event!</error>)"></span>

    <footer>{{<error descr="TS7053: Element implicitly has an 'any' type because expression of type '0' can't be used to index type 'Person'.
  Property '0' does not exist on type 'Person'." textAttributesKey="ERRORS_ATTRIBUTES"><error descr="TS2531: Object is possibly 'null'." textAttributesKey="ERRORS_ATTRIBUTES">(personPromise | async)</error>[0]</error>}}</footer> <!-- ensure that null checks work -->
  `,
})
export class TestComponentTwo extends <error descr="TS2449: Class 'TestComponentBase' used before its declaration." textAttributesKey="ERRORS_ATTRIBUTES">TestComponentBase</error> {
}

abstract class TestComponentBase {
  simpleField: number | null = 11;

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

@Directive({
  selector: '[appAgree]',
  standalone: true
})
export class AgreeDirective<T> {
  @Input() appAgree!: T;
  @Output() appAgreeChange = new EventEmitter<T>();
  @Input() appAgreeSecond!: T | undefined | null;
}

@Directive({
  selector: '[appAgree]',
  standalone: true
})
export class AgreeDirectiveDuplicate<T> {
  @Input() appAgree: T | undefined | null;
  @Output() appAgreeChange = new EventEmitter<T | undefined | null>();
}
