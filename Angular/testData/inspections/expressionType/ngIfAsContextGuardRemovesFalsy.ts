// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core";
import {CommonModule} from "@angular/common";

export interface Person {
  familyName: string;
}

@Component({
  selector: 'app-test',
  imports: [CommonModule],
  standalone: true,
  template: `
    {{ blank.indexOf("") }}
    {{ getPersonOrSomething().<error descr="TS2339: Property 'familyName' does not exist on type 'false | \"\" | Person'.
  Property 'familyName' does not exist on type 'false'.">familyName</error> }}
    {{ getPersonOrSomething().<error descr="TS2339: Property 'indexOf' does not exist on type 'false | \"\" | Person'.
  Property 'indexOf' does not exist on type 'false'.">indexOf</error>("") }}
    <div *ngIf="getPersonOrSomething() as person1; let person2">
      {{ person1.familyName }}
      {{ person2.familyName }}
      {{ expectPerson(person1) }}
      {{ expectPerson(person2) }}
      {{ person1.<error descr="TS2339: Property 'indexOf' does not exist on type 'Person'.">indexOf</error>("") }}
      {{ person2.<error descr="TS2339: Property 'indexOf' does not exist on type 'Person'.">indexOf</error>("") }}
      {{ expectNumber(<error descr="TS2345: Argument of type 'Person' is not assignable to parameter of type 'number'.">person1</error>) }}
      {{ expectNumber(<error descr="TS2345: Argument of type 'Person' is not assignable to parameter of type 'number'.">person2</error>) }}
    </div>
  `,
})
export class TestComponent {
  getPersonOrSomething = (): Person | false | "" => ({
    familyName: 'Doe'
  });

  blank = "" as const;

  expectPerson(num: Person): Person {
    return num;
  }

  expectNumber(num: number): number {
    return num;
  }
}
