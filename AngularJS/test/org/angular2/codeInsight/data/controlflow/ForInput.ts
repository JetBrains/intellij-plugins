// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-flow',
  imports: [CommonModule],
  standalone: true,
  template: `
    <div *ngIf="isString(bar)">
      <div *ngFor="let letter of bar">{{letter}}</div>
    </div>
    <div *ngFor="let letter of <error descr="Type string | number is not assignable to type (string & NgIterable<string>) | (number & NgIterable<string>) | undefined | null...  Type string | number is not assignable to type (string & Array<string>) | (string & Iterable<string>) | (number & Array<string>) | (number & Iterable<string>) | undefined | null">bar</error>">{{letter}}</div>
  `,
})
export class FlowComponent {
  bar: string | number = 'hello';

  isString(x: unknown): x is string {
    return typeof x === "string";
  }
}
