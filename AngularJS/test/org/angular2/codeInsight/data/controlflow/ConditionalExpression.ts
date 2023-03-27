// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-flow',
  imports: [CommonModule],
  standalone: true,
  template: `
    {{isString(bar) ? bar.length : bar.<error descr="Unresolved variable length">length</error>}}
  `,
})
export class FlowComponent {
  bar: string | number = 'hello';

  isString(x: unknown): x is string {
    return typeof x === "string";
  }
}
