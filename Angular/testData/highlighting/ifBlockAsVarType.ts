// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Component } from '@angular/core';

@Component({
 selector: 'todos',
 template: `
    @if (foo; as bar)  {
      {{ test(<error descr="TS2345: Argument of type '\"a\" | \"b\"' is not assignable to parameter of type '\"f\"'.
  Type '\"a\"' is not assignable to type '\"f\"'.">bar</error>) }}
    } @else {
      {{ test(<error descr="TS2339: Property 'bar' does not exist on type 'ComponentStoreTodosComponent'.">bar</error>) }}
      TCB incorrectly generates the if block when alias is present, so foo is not properly narrowed here. 
      {{ test(<error descr="TS2345: Argument of type '\"\" | \"a\" | \"b\" | undefined' is not assignable to parameter of type '\"f\"'.
  Type 'undefined' is not assignable to type '\"f\"'.">foo</error>) }}
    }
  `,
   standalone: true,
 })
export class ComponentStoreTodosComponent {

  foo!: 'a' | 'b' | '' | undefined;

  test(a: 'f'): boolean {
    return a === 'f'
  }

}
