// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input} from '@angular/core';


@Component({
    selector: 'null-checks',
    template: `
        {{bar.<error descr="Qualifier of 'length' is possibly undefined">length</error>}}
        {{bar.<error descr="Unresolved variable unresolved">unresolved</error>}}
        <div [title]="bar.<error descr="Qualifier of 'length' is possibly undefined">length</error>"></div>
        {{acceptString(<error descr="Argument type string | undefined is not assignable to parameter type string  Type undefined is not assignable to type string">bar</error>)}}
        <null-checks [foo]="<error descr="Type string | undefined is not assignable to type string  Type undefined is not assignable to type string">bar</error>"></null-checks>
    `
})
export class TestComponent {
  bar: string | undefined = (() => undefined)();

  @Input foo: string;

  acceptString(x: string): string {
    return x;
  }
}
