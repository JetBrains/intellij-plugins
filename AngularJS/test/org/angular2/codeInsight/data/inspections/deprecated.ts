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

@Component({
 standalone: true,
 selector: 'app-test2',
 imports: [
   FooComponent,
   BarComponent,
   BarDirective,
   FooBarDirective
 ],
 template:`
    <app-foo <weak_warning descr="fooCmp is deprecated, consult docs for better alternative">fooCmp</weak_warning>="" [fooCmp2]=""></app-foo>
    <app-foo [<weak_warning descr="fooCmp is deprecated, consult docs for better alternative">fooCmp</weak_warning>]="" fooCmp2=""></app-foo>
    <<warning descr="app-bar is deprecated. Do not use!">app-bar</warning> ></<warning descr="app-bar is deprecated. Do not use!">app-bar</warning>>
    <<warning descr="app-bar is deprecated. Do not use!">app-bar</warning> [<weak_warning descr="bar is deprecated. Do not use!">bar</weak_warning>]="'12'" <weak_warning descr="barCmp is deprecated. Do not use!">barCmp</weak_warning>="111"></<warning descr="app-bar is deprecated. Do not use!">app-bar</warning>>

    <div <weak_warning descr="bar is deprecated. Do not use!">bar</weak_warning>="12" [<weak_warning descr="bar2 is deprecated. Do not use!">bar2</weak_warning>]="'12'" <warning descr="Attribute fff is not allowed here">fff</warning>="12"></div>
    <div fooBar="12" [<weak_warning descr="fooBar2 is deprecated. Do not use!">fooBar2</weak_warning>]="'12'"></div>
 `
})
export class TestComponentInline {

}
