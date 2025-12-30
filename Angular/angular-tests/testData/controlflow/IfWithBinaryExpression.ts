// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-flow',
  imports: [CommonModule],
  standalone: true,
  template: `
    <div *ngIf='isString(bar) && true'>
      <div [title]='bar.length'></div>
      <div [title]='bar.<error descr="Unresolved variable unresolved">unresolved</error>'></div>
      <div [title]='true || false'></div>
      <div [title]="bar.length !== 5"></div>
    </div>
    <div [title]='bar.<error descr="Unresolved variable length">length</error>'></div>
    <div [title]='isString(bar) && bar.length'></div>
    <div [title]='isString(bar) ? bar.length : bar.<error descr="Unresolved variable length">length</error>'></div>
    <div *ngIf='isString(bar) && bar.length'>
      <div [title]="bar.length !== 5"></div>
    </div>
  `,
})
export class FlowComponent {
  bar: string | number = 'hello';

  isString(x: unknown): x is string {
    return typeof x === "string";
  }
}
