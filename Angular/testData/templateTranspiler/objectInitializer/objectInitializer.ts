// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core";
import {CommonModule} from "@angular/common";
import {Observable, of} from "rxjs";
import {LetDirective} from "./let.directive";

@Component({
  selector: 'app-test',
  imports: [CommonModule, LetDirective],
  standalone: true,
  template: `
    <ng-container *ngrxLet="{ number1: number$, number2: numberOrNot$ } as vm">
      <div>{{vm.number1.toFixed()}}</div>
    </ng-container>
  `,
})
export class TestComponent {
  loginState$: Observable<number> = of(1);

  number$: Observable<number> = this.loginState$;
  numberOrNot$: Observable<number | undefined> = this.loginState$;
}
