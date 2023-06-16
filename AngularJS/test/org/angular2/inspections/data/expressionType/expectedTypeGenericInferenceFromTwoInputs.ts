// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, EventEmitter, Input, Output} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test-one',
  imports: [CommonModule, AgreeDirective],
  standalone: true,
  template: `
    <span [appAgree]="<error descr="Type  null  is not assignable to type  number ">null</error>" [appAgreeSecond]="simpleField"></span> <!-- todo no errors, bug in TS -->
    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField"></span>
    <span [(appAgree)]="simpleField" [appAgreeSecond]="simpleField"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async"></span>

    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField" (appAgreeChange)="expectNumber(<error descr="Argument type  number | null  is not assignable to parameter type  number   Type  null  is not assignable to type  number ">$event</error>)"></span>
    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField" (appAgreeChange)="expectNumber($event!)"></span>
    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField" (appAgreeChange)="expectPerson(<error descr="Argument type  number  is not assignable to parameter type  Person ">$event!</error>)"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async" (appAgreeChange)="expectPerson(<error descr="Argument type  Person | null  is not assignable to parameter type  Person   Type  null  is not assignable to type  Person ">$event</error>)"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async" (appAgreeChange)="expectPerson($event!)"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async" (appAgreeChange)="expectNumber(<error descr="Argument type  Person  is not assignable to parameter type  number ">$event!</error>)"></span>

    <footer>{{<error descr="Indexed expression can be null or undefined">(personPromise | async)</error>[0]}}</footer> <!-- ensure that null checks work -->
  `,
})
export class TestComponentOne extends TestComponentBase {
}

// including AgreeDirectiveDuplicate previously produced still wrong errors, though slightly differently wrong
@Component({
  selector: 'app-test-two',
  imports: [CommonModule, AgreeDirective, AgreeDirectiveDuplicate],
  standalone: true,
  template: `
    <span [appAgree]="null" [appAgreeSecond]="simpleField"></span>
    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField"></span>
    <span [(appAgree)]="simpleField" [appAgreeSecond]="simpleField"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async"></span>

    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField" (appAgreeChange)="expectNumber(<error descr="Argument type  number | null  is not assignable to parameter type  number   Type  null  is not assignable to type  number ">$event</error>)"></span>
    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField" (appAgreeChange)="expectNumber($event!)"></span>
    <span [appAgree]="simpleField" [appAgreeSecond]="simpleField" (appAgreeChange)="expectPerson(<error descr="Argument type  number  is not assignable to parameter type  Person ">$event!</error>)"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async" (appAgreeChange)="expectPerson(<error descr="Argument type Person | (Person & undefined) | (Person & null) | (null & Person) | null is not assignable to parameter type  Person   Type  null  is not assignable to type  Person ">$event</error>)"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async" (appAgreeChange)="expectPerson($event!)"></span>
    <span [appAgree]="personPromise | async" [appAgreeSecond]="personPromise | async" (appAgreeChange)="expectNumber(<error descr="Argument type Person | (Person & undefined) | (Person & null) | (null & Person) is not assignable to parameter type  number   Type  Person  is not assignable to type  number ">$event!</error>)"></span>

    <footer>{{<error descr="Indexed expression can be null or undefined">(personPromise | async)</error>[0]}}</footer> <!-- ensure that null checks work -->
  `,
})
export class TestComponentTwo extends TestComponentBase {
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
