// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input} from '@angular/core';
import {CommonModule} from "@angular/common";

@Component({
  selector: 'null-checks',
  imports: [CommonModule],
  standalone: true,
  template: `
    {{bar.<error descr="Qualifier of 'length' is possibly undefined">length</error>}}
    {{bar.<error descr="Unresolved variable unresolved">unresolved</error>}}
    <div [title]="bar.<error descr="Qualifier of 'length' is possibly undefined">length</error>"></div>
    {{acceptString(<error descr="Argument type string | undefined is not assignable to parameter type string  Type undefined is not assignable to type string">bar</error>)}}
    <null-checks [foo]="<error descr="Type string | undefined is not assignable to type string  Type undefined is not assignable to type string">bar</error>"></null-checks>
    <null-checks [foo]="<error descr="Type string | null is not assignable to type string  Type null is not assignable to type string">bazPromise | async</error>"></null-checks>
    <null-checks [foo]="(bazPromise | async)!"></null-checks>
  `
})
export class TestComponent {
  @Input foo!: string;

  bar: string | undefined = (() => undefined)();

  bazPromise = Promise.resolve("hello");

  acceptString(x: string): string {
    return x;
  }
}
