// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input, OnInit} from '@angular/core';

@Component({
  standalone: true,
  selector: 'app-foo',
  template: ``,
})
export class FooComponent {
  @Input()
  /**
   * @deprecated
   */
  fooCmp = '';

  @Input()
  fooCmp2 = '';

}

/**
 * @deprecated do not use!
 */
@Component({
  standalone: true,
  selector: 'app-bar',
  template: ``,
})
export class BarComponent {

  @Input()
  barCmp = '';

}

/**
 * @deprecated do not use!
 */
@Directive({
  standalone: true,
  selector: '[bar]',
})
export class BarDirective{

  @Input()
  bar = '';

  @Input()
  bar2 = '';
}

@Directive({
  standalone: true,
  selector: '[fooBar]',
})
export class FooBarDirective {

  @Input()
  fooBar = '';

  /**
   * @deprecated do not use!
   */
  @Input()
  fooBar2 = '';

}

@Component({
  standalone: true,
  selector: 'app-test',
  imports: [
    FooComponent,
    BarComponent,
    BarDirective,
    FooBarDirective
  ],
  templateUrl: "./deprecated.html",
})
export class TestComponent {

}