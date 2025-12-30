// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-flow',
  imports: [CommonModule],
  standalone: true,
  template: `
    <div *ngFor="let item of items">
      <div>{{isString(item) && item.length}}</div>
      <div>{{isString(item) && item.<error descr="Unresolved function or method toFixed()">toFixed</error>()}}</div>
    </div>
  `,
})
export class FlowComponent {
  items: (string | number)[] = ['hello'];

  isString(x: unknown): x is string {
    return typeof x === "string";
  }
}
