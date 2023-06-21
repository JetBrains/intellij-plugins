// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';
import {LetDirective} from "./ngrxLet";

interface Person {
  familyName: string;
}

@Component({
  selector: 'app-example',
  standalone: true,
  imports: [
    LetDirective
  ],
  template: `
    <ng-container *ngrxLet="personPromise as person; let person2">
      {{ expectPerson(person) }}
      {{ expectPerson(person2) }}
      {{ expectNumber(<error descr="Argument type LetViewContextValue<Promise<Awaited<{    familyName: string}>>> is not assignable to parameter type  number ">person</error>) }}
      {{ expectNumber(<error descr="Argument type LetViewContextValue<Promise<Awaited<{    familyName: string}>>> is not assignable to parameter type  number ">person2</error>) }}
    </ng-container>
  `
})
export class Example {
  personPromise = Promise.resolve({
    familyName: 'Doe'
  });

  expectPerson(num: Person): Person {
    return num;
  }

  expectNumber(num: number): number {
    return num;
  }
}