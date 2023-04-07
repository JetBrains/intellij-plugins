// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core";
import {CommonModule} from "@angular/common";

export interface Person {
  familyName: string;
}

@Component({
  selector: 'app-flow',
  imports: [CommonModule],
  standalone: true,
  template: `
    <div *ngIf="personPromise | async as person; let person2">
      {{ person.familyName }}
      {{ person2.familyName }}
      {{ expectPerson(person) }}
      {{ expectPerson(person2) }}
      {{ expectNumber(<error descr="Argument type Person is not assignable to parameter type number">person</error>) }}
      {{ expectNumber(<error descr="Argument type Person is not assignable to parameter type number">person2</error>) }}
    </div>
  `,
})
export class FlowComponent {
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
