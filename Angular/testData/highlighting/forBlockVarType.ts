// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Component } from '@angular/core';

@Component({
 selector: 'todos',
 template: `
    @for (foo of bar; track foo.<error descr="TS2339: Property 'id' does not exist on type '{ name: string; }'.">id</error>)  {
      {{ foo.name }} - {{ foo.<error descr="TS2339: Property 'address' does not exist on type '{ name: string; }'.">address</error> }}
    }
  `,
   standalone: true,
 })
export class ComponentStoreTodosComponent {

  bar!: { name: string }[];

}
