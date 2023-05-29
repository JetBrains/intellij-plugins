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
    <ng-container *ngrxLet="number$ as number">{{number.toFixed()}}</ng-container>
    <ng-container *ngrxLet="number$OrNot as number">{{number.<error descr="Unresolved function or method toFixed()">toFixed</error>()}}</ng-container>
    <ng-container *ngrxLet="numberOrNot$ as number">{{number.<error descr="Qualifier of 'toFixed' is possibly undefined">toFixed</error>()}}</ng-container>
    
    <ng-container *ngrxLet="number$ as n1; let n2; error as e; complete as c">
      {{n1.toFixed()}}
      {{n2.toFixed()}}
      {{acceptBoolean(e)}}
      {{acceptBoolean(c)}}
      {{acceptBoolean(<error descr="Argument type number is not assignable to parameter type boolean">n1</error>)}}
    </ng-container>
    
    <ng-container *ngrxLet="{ number1: number$, number2: numberOrNot$ } as vm">
      <div>{{vm.number1.toFixed()}}</div>
      <div>{{vm.number2.<error descr="Qualifier of 'toFixed' is possibly undefined">toFixed</error>()}}</div>
    </ng-container>
    
    <!-- can't pass nullable Observable in the "Combining Multiple Observables" mode-->
    <ng-container *ngrxLet="{ number1: number$, number2: numberOrNot$, number3: number$OrNot } as vm">
      <div>{{vm.number1.<error descr="Unresolved function or method toFixed()">toFixed</error>()}}</div>
      <div>{{vm.number2.<error descr="Unresolved function or method toFixed()">toFixed</error>()}}</div>
      <div>{{vm.number3.<error descr="Unresolved function or method toFixed()">toFixed</error>()}}</div>
    </ng-container>
    
    <ng-container *ngrxLet="{ number$: deep$ } as vm">
      <div>{{vm.number$.<error descr="Unresolved function or method toFixed()">toFixed</error>()}}</div>
      <div>{{vm.number$.subscribe()}}</div>
    </ng-container>
    
    <ng-container *ngrxLet="deep$ as number$">
      <div>{{number$.<error descr="Unresolved function or method toFixed()">toFixed</error>()}}</div>
      <div>{{number$.subscribe()}}</div>
    </ng-container>
    
    <ng-container *ngrxLet="foo.bar.baz as baz">{{baz?.toUpperCase()}}</ng-container>
    <ng-container *ngrxLet="foo.bar.baz as baz">{{baz.<error descr="Qualifier of 'toUpperCase' is possibly undefined">toUpperCase</error>()}}</ng-container>
  `,
})
export class TestComponent {
  loginState$: Observable<number> = of(1);

  number$: Observable<number> = this.loginState$;
  number$OrNot: Observable<number> | undefined = this.loginState$;
  numberOrNot$: Observable<number | undefined> = this.loginState$;

  deep$: Observable<Observable<number>> = of(this.number$);

  foo = {
    bar: {
      baz: "hello" as string | undefined
    }
  }

  acceptBoolean(value: boolean): boolean {
    return value
  }
}
