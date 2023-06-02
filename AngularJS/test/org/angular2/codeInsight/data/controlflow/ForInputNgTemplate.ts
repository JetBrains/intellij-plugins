// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-flow',
  imports: [CommonModule],
  standalone: true,
  template: `
    <ng-template [ngIf]="isString(bar)">
      <ng-template ngFor let-letter [ngForOf]="bar">{{letter}}</ng-template>
      <ng-template ngFor [ngForOf]="bar" let-letter>{{letter}}</ng-template>
    </ng-template>
    <ng-template ngFor let-letter [ngForOf]="<error descr="Type  string | number  is not assignable to type (string & NgIterable<string>) | (number & NgIterable<string>) | undefined | null...  Type string | number is not assignable to type (string & Array<string>) | (string & Iterable<string>) | (number & Array<string>) | (number & Iterable<string>) | undefined | null">bar</error>">{{letter}}</ng-template>
  `,
})
export class FlowComponent {
  bar: string | number = 'hello';

  isString(x: unknown): x is string {
    return typeof x === "string";
  }
}
