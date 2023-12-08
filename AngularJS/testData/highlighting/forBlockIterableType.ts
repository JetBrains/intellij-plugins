// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Component } from '@angular/core';

@Component({
 selector: 'todos',
 template: `
    @for (foo of iter; track $index)  {}
    @for (foo of iterNull; track $index)  {}
    @for (foo of <error descr="Type { name: string } must have a [Symbol.iterator]() method that returns an iterator.">nonIter</error>; track $index)  {}
  `,
   standalone: true,
 })
export class ComponentStoreTodosComponent {

  iter!: { name: string }[];
  iterNull!: { name: string }[] | null | undefined;
  nonIter!: { name: string };

}
