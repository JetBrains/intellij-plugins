// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Component } from '@angular/core';

@Component({
 selector: 'todos',
 template: `
    @if (foo) {
      @switch (foo) {
        @case ('b')  {
          {{ test(<error descr="Argument type \"b\" is not assignable to parameter type \"f\"">foo</error>) }}
        } 
        @case ('d') {
          {{ test(<error descr="Argument type \"a\" is not assignable to parameter type \"f\"">foo</error>) }}
        } 
        @case ('a') {
          {{ test(<error descr="Argument type \"a\" is not assignable to parameter type \"f\"">foo</error>) }}
        } 
        @default {
          {{ test(foo) }}
        }
      }
      {{ test(<error descr="Argument type \"a\" | \"b\" is not assignable to parameter type \"f\"  Type \"a\" is not assignable to type \"f\"">foo</error>) }}
    } @else {
      {{ test(<error descr="Argument type undefined is not assignable to parameter type \"f\"">foo</error>) }}
    }
  `,
   standalone: true,
 })
export class ComponentStoreTodosComponent {

  foo!: 'a' | 'b' | undefined;

  test(a: 'f'): boolean {
    return a === 'f'
  }

  constructor() {
    if (this.foo) {
      this.test(<error descr="Argument type \"a\" | \"b\" is not assignable to parameter type \"f\"  Type \"a\" is not assignable to type \"f\"">this.foo</error>);
    } else {
      this.test(<error descr="Argument type undefined is not assignable to parameter type \"f\"">this.foo</error>);
    }
  }
}
