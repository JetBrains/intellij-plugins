// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Component } from '@angular/core';

@Component({
 selector: 'todos',
 template: `
    @for (foo of bar; track foo.<error descr="Unresolved variable id">id</error>)  {
      {{ foo.name }} - {{ foo.<error descr="Unresolved variable address">address</error> }}
    }
  `,
   standalone: true,
 })
export class ComponentStoreTodosComponent {

  bar!: { name: string }[];

}
