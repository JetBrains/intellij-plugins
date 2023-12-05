// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Component } from '@angular/core';

@Component({
 selector: 'todos',
 template: `
    @if (foo; as bar)  {
      {{ test(<error descr="Argument type \"a\" | \"b\" is not assignable to parameter type \"f\"  Type \"a\" is not assignable to type \"f\"">bar</error>) }}
    } @else {
      {{ test(<error descr="Unresolved variable or type bar">bar</error>) }}
      {{ test(<error descr="Argument type \"\" | undefined is not assignable to parameter type \"f\"  Type \"\" is not assignable to type \"f\"">foo</error>) }}
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
