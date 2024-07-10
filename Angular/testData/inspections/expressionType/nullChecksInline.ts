// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input} from '@angular/core';
import {CommonModule} from "@angular/common";

@Component({
  selector: 'null-checks',
  imports: [CommonModule],
  standalone: true,
  template: `
    {{<error descr="TS2532: Object is possibly 'undefined'.">bar</error>.length}}
    {{<error descr="TS2532: Object is possibly 'undefined'.">bar</error>.<error descr="TS2339: Property 'unresolved' does not exist on type 'string'.">unresolved</error>}}
    <div [title]="<error descr="TS2532: Object is possibly 'undefined'.">bar</error>.length"></div>
    {{acceptString(<error descr="TS2345: Argument of type 'string | undefined' is not assignable to parameter of type 'string'.
  Type 'undefined' is not assignable to type 'string'.">bar</error>)}}
    <null-checks <error descr="TS2322: Type 'string | undefined' is not assignable to type 'string'.
  Type 'undefined' is not assignable to type 'string'.">[foo]</error>="bar"></null-checks>
    <null-checks <error descr="TS2322: Type 'string | null' is not assignable to type 'string'.
  Type 'null' is not assignable to type 'string'.">[foo]</error>="bazPromise | async"></null-checks>
    <null-checks [foo]="(bazPromise | async)!"></null-checks>
  `
})
export class TestComponent {
  @Input() foo!: string;

  bar: string | undefined = (() => undefined)();

  bazPromise = Promise.resolve("hello");

  acceptString(x: string): string {
    return x;
  }
}
