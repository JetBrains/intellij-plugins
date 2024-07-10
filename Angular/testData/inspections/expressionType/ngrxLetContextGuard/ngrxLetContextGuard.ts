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
    <ng-container *ngrxLet="number$OrNot as number">{{<error descr="TS18048: 'number' is possibly 'undefined'.">number</error>.toFixed()}}</ng-container>
    <ng-container *ngrxLet="numberOrNot$ as number">{{<error descr="TS18048: 'number' is possibly 'undefined'.">number</error>.toFixed()}}</ng-container>
    
    <ng-container *ngrxLet="number$ as n1; let n2; error as e; complete as c">
      {{n1.toFixed()}}
      {{n2.toFixed()}}
      {{acceptBoolean(e)}}
      {{acceptBoolean(c)}}
      {{acceptBoolean(<error descr="TS2345: Argument of type 'number' is not assignable to parameter of type 'boolean'.">n1</error>)}}
    </ng-container>
    
    <ng-container *ngrxLet="{ number1: number$, number2: numberOrNot$ } as vm">
      <div>{{vm.number1.toFixed()}}</div>
      <div>{{<error descr="TS18048: 'vm.number2' is possibly 'undefined'.">vm.number2</error>.toFixed()}}</div>
    </ng-container>
    
    <!-- can't pass nullable Observable in the "Combining Multiple Observables" mode-->
    <ng-container *ngrxLet="{ number1: number$, number2: numberOrNot$, number3: number$OrNot } as vm">
      <div>{{vm.number1.<error descr="TS2339: Property 'toFixed' does not exist on type 'Observable<number>'.">toFixed</error>()}}</div>
      <div>{{vm.number2.<error descr="TS2339: Property 'toFixed' does not exist on type 'Observable<number | undefined>'.">toFixed</error>()}}</div>
      <div>{{<error descr="TS18048: 'vm.number3' is possibly 'undefined'.">vm.number3</error>.<error descr="TS2339: Property 'toFixed' does not exist on type 'Observable<number>'.">toFixed</error>()}}</div>
    </ng-container>
    
    <ng-container *ngrxLet="{ number$: deep$ } as vm">
      <div>{{vm.number$.<error descr="TS2339: Property 'toFixed' does not exist on type 'Observable<number>'.">toFixed</error>()}}</div>
      <div>{{vm.number$.subscribe()}}</div>
    </ng-container>
    
    <ng-container *ngrxLet="deep$ as number$">
      <div>{{number$.<error descr="TS2339: Property 'toFixed' does not exist on type 'Observable<number>'.">toFixed</error>()}}</div>
      <div>{{number$.subscribe()}}</div>
    </ng-container>
    
    <ng-container *ngrxLet="foo.bar.baz as baz">{{baz?.toUpperCase()}}</ng-container>
    <ng-container *ngrxLet="foo.bar.baz as baz">{{<error descr="TS18048: 'baz' is possibly 'undefined'.">baz</error>.toUpperCase()}}</ng-container>
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
