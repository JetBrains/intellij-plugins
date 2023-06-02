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
    {{ getPersonOrSomething().<error descr="Unresolved variable familyName">familyName</error> }}
    {{ getPersonOrSomething().<error descr="Unresolved function or method indexOf()">indexOf</error>("") }}
    <div *ngIf="getPersonOrSomething() as person1; let person2">
      {{ person1.familyName }}
      {{ person2.familyName }}
      {{ expectPerson(person1) }}
      {{ expectPerson(person2) }}
      {{ person1.<error descr="Unresolved function or method indexOf()">indexOf</error>("") }}
      {{ person2.<error descr="Unresolved function or method indexOf()">indexOf</error>("") }}
      {{ expectNumber(<error descr="Argument type  Person  is not assignable to parameter type  number ">person1</error>) }}
      {{ expectNumber(<error descr="Argument type  Person  is not assignable to parameter type  number ">person2</error>) }}
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
