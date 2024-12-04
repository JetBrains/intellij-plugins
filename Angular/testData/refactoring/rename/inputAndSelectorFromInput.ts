// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input} from "@angular/core"

@Directive({
 selector: '[my-input]',
 standalone: true,
})
export class FooDirective {
  @Input("my-<caret>input")
  myInput!: string;
}

@Component({
 selector: 'app-root',
 imports: [FooDirective],
 template: `
    <div my-input="foo"></div>
  `,
 standalone: true,
})
export class AppComponent {
}