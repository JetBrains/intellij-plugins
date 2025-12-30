// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test',
  imports: [CommonModule],
  standalone: true,
  template: `
    <div *ngFor="let person of personsPromise | async as iterable; index as i">
      {{i.toFixed()}}
      {{expectPerson(person)}}
      {{expectNumber(<error descr="TS2345: Argument of type 'Person' is not assignable to parameter of type 'number'.">person</error>)}}
      {{expectNumber(<error descr="TS2345: Argument of type 'Person[]' is not assignable to parameter of type 'number'.">iterable</error>)}}
      {{expectPerson(iterable[0])}}
    </div>
    <div *ngFor="let person of personsPromise | async as iterable; index as i; trackBy: trackByPerson">
      {{i.toFixed()}}
      {{expectPerson(person)}}
      {{expectNumber(<error descr="TS2345: Argument of type 'Person' is not assignable to parameter of type 'number'.">person</error>)}}
      {{expectNumber(<error descr="TS2345: Argument of type 'Person[]' is not assignable to parameter of type 'number'.">iterable</error>)}}
      {{expectPerson(iterable[0])}}
    </div>
    <div *ngFor="let person of personsPromise | async as iterable; index as i; trackBy: trackByEntity">
      {{i.toFixed()}}
      {{expectPerson(person)}}
      {{expectNumber(<error descr="TS2345: Argument of type 'Person' is not assignable to parameter of type 'number'.">person</error>)}}
      {{expectNumber(<error descr="TS2345: Argument of type 'Person[]' is not assignable to parameter of type 'number'.">iterable</error>)}}
      {{expectPerson(iterable[0])}}
    </div>
    <footer>{{<error descr="TS2531: Object is possibly 'null'.">(personsPromise | async)</error>[0]}}</footer> <!-- ensure that null checks work -->
  `,
})
export class TestComponent {
  personsPromise = Promise.resolve<Person[]>([{
    id: 1,
    familyName: 'Doe'
  }]);

  trackByEntity = (_index: number, item: Entity): any => {
    return item.id;
  }

  trackByPerson = (_index: number, item: Person): any => {
    return item.familyName;
  }

  expectPerson(num: Person): Person {
    return num;
  }

  expectNumber(num: number): number {
    return num;
  }
}

export interface Entity {
  id: number;
}

export interface Person extends Entity {
  familyName: string;
}