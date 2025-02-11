// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input, <weak_warning descr="TS6133: 'OnInit' is declared but its value is never read.">OnInit</weak_warning>} from '@angular/core';

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
  selector: '[<weak_warning descr="bar is deprecated, do not use!">bar</weak_warning>]',
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
   <weak_warning descr="TS6385: 'BarComponent' is deprecated.">BarComponent</weak_warning>,
   <weak_warning descr="TS6385: 'BarDirective' is deprecated.">BarDirective</weak_warning>,
   FooBarDirective
 ],
 template:`
    <app-foo <weak_warning descr="fooCmp is deprecated, consult docs for better alternative">fooCmp</weak_warning>="" <error descr="TS2322: Type 'undefined' is not assignable to type 'string'.">[fooCmp2]</error>=""></app-foo>
    <app-foo <error descr="TS2322: Type 'undefined' is not assignable to type 'string'.">[<weak_warning descr="fooCmp is deprecated, consult docs for better alternative">fooCmp</weak_warning>]</error>="" fooCmp2=""></app-foo>
    <<warning descr="app-bar is deprecated, do not use!">app-bar</warning> ></<warning descr="app-bar is deprecated, do not use!">app-bar</warning>>
    <<warning descr="app-bar is deprecated, do not use!">app-bar</warning> [<weak_warning descr="bar is deprecated, do not use!">bar</weak_warning>]="'12'" <weak_warning descr="barCmp is deprecated, do not use!">barCmp</weak_warning>="111"></<warning descr="app-bar is deprecated, do not use!">app-bar</warning>>

    <div <weak_warning descr="bar is deprecated, do not use!">bar</weak_warning>="12" [<weak_warning descr="bar2 is deprecated, do not use!">bar2</weak_warning>]="'12'" <warning descr="Attribute fff is not allowed here">fff</warning>="12"></div>
    <div fooBar="12" <weak_warning descr="TS6385: 'fooBar2' is deprecated.">[<weak_warning descr="fooBar2 is deprecated, do not use!">fooBar2</weak_warning>]</weak_warning>="'12'"></div>
 `
})
export class TestComponentInline {

}
