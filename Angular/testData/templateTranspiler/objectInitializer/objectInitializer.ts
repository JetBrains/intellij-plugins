// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Input} from "@angular/core";
import {CommonModule} from "@angular/common";
import {Observable, of} from "rxjs";
import {LetDirective} from "./let.directive";

@Component({
  inputs: [
    { name: "obj1", transform: (value: { foo1: number }): boolean => Boolean(value) },
  ],
  selector: 'app-test2',
  standalone: true,
  template: ``
})
export class TestComponent2 {
  @Input({transform: (value: { foo2: number }): boolean => Boolean(value)})
  obj2!: boolean;

  @Input()
  obj3!: { foo3: number };
}

@Component({
  selector: 'app-test',
  imports: [CommonModule, LetDirective, TestComponent2],
  standalone: true,
  template: `
    <ng-container *ngrxLet="{ number1: number$, number2: numberOrNot$ } as vm">
      <div>{{vm.number1.toFixed()}}</div>
    </ng-container>
    <app-test2
            [obj1]="{foo1: 12}"
            [obj2]="{foo2: 12}"
            [obj3]="{foo3: 12}"
    ></app-test2>
  `,
})
export class TestComponent {
  loginState$: Observable<number> = of(1);

  number$: Observable<number> = this.loginState$;
  numberOrNot$: Observable<number | undefined> = this.loginState$;
}