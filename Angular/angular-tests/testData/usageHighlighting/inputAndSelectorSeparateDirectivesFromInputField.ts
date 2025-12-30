// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input} from "@angular/core"

@Directive({
 selector: '[formControlName]',
 standalone: true,
})
export class FooDirective {
}

@Directive({
 selector: '[<usage>formControlName</usage>]',
 standalone: true,
})
export class BarDirective {
  @Input()
  <usage>form<caret>ControlName</usage>!: string;
}

@Component({
 selector: 'app-root',
 imports: [FooDirective, BarDirective],
 template: `
    <div <usage>formControlName</usage>="foo"></div>
  `,
 standalone: true,
})
export class AppComponent {
}