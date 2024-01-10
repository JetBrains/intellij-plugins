// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Component, Input } from '@angular/core';

@Component({
   selector: 'my-component',
   standalone: true,
   template: ``,
 })
export class MyComponent {
  @Input() input = '';
}

@Component({
   selector: 'app-root',
   standalone: true,
   imports: [MyComponent],
   template: `
    <my-component [input]="name"></my-component>
    <my-component [input]=""></my-component>
    <my-component <warning descr="[input] requires value">[input]</warning>></my-component>
  `,
})
export class AppComponent {
  name: string | null = 'Angular';
}
