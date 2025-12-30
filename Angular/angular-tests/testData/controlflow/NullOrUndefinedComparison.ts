// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Input} from '@angular/core';
import {CommonModule} from "@angular/common";

@Component({
   selector: 'app-root',
   imports: [CommonModule],
   standalone: true,
   template: `
    <div *ngIf="myVar !== undefined" (click)="myFun(<error descr="Argument type number | null is not assignable to parameter type number  Type null is not assignable to type number">myVar</error>)"></div>
    <div *ngIf="myVar !== null" (click)="myFun(<error descr="Argument type undefined | number is not assignable to parameter type number  Type undefined is not assignable to type number">myVar</error>)"></div>
    <div *ngIf="myVar !== null && myVar !== undefined" (click)="myFun(myVar)"></div>
    <div *ngIf="myVar != undefined" (click)="myFun(myVar)"></div>
    <div *ngIf="myVar != null" (click)="myFun(myVar)"></div>
    <div *ngIf="myVar != null && myVar != undefined" (click)="myFun(myVar)"></div>
   `
})
export class AppComponent {

  myVar: number | undefined | null = 5

  myFun(<warning descr="Unused parameter v">v</warning>: number): void {
  }

}