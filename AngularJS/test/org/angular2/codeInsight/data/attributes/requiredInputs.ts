// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input, Output, ViewContainerRef, TemplateRef} from '@angular/core';


@Directive({
             standalone:true,
             selector: '[myRequiredInput],[mySelector]'
           })
export class TestDirective {
  @Input({required: true, alias: "myRequiredInput"})
  myInput;
}

@Directive({
             standalone:true,
             selector: '[myOptionalInput],[myOtherSelector]'
           })
export class TestDirective2 {
  @Input({required: false, alias: "myOptionalInput"})
  myInput;
}

@Directive({
             standalone:true,
             selector: '[myThirdSelector]'
           })
export class TestDirective3 {
  @Input({required: true, alias: "theRequiredInput"})
  myInput;
}

@Directive({
             selector: '[ngFor]',
             standalone: true,
           })
export class NgForOf {

  @Input({required: true})
  set ngForOf(<warning descr="Unused parameter ngForOf">ngForOf</warning>: any) {
  }

  @Input()
  set ngForTrackBy(<warning descr="Unused parameter fn">fn</warning>: any) {
  }

  get ngForTrackBy(): any {
    return {};
  }

  constructor(
    private _viewContainer: ViewContainerRef,
    private _template: TemplateRef<any>,
  ) {}

  @Input()
  set ngForTemplate(<warning descr="Unused parameter value">value</warning>: any) {
  }

}

@Component({
  standalone:true,
  imports: [
    TestDirective,
    TestDirective2,
    TestDirective3,
    NgForOf
  ],
  template: `
    <div
      <warning descr="myRequiredInput requires value">myRequiredInput</warning>
    ></div>
    <div
      myRequiredInput="foo"
    ></div>
    <<error descr="Missing binding for required input myRequiredInput of directive TestDirective">div</error>
      mySelector
    ></div>
    <div
      myOptionalInput
    ></div>
    <div
      myOptionalInput="foo"
    ></div>
    <div
      myOtherSelector
    ></div>
    <<error descr="Missing binding for required input theRequiredInput of directive TestDirective3">div</error>
      myThirdSelector
    ></div>
    <div
      myThirdSelector
      <warning descr="theRequiredInput requires value">theRequiredInput</warning>
    ></div>
    <div
      myThirdSelector
      theRequiredInput="foo"
    ></div>
    <div *ngFor="let a of b"></div>
    <div <error descr="Missing binding for required input ngForOf of directive NgForOf">*ngFor</error>="let a"></div>
    <div
      <warning descr="Attribute foo is not allowed here">foo</warning>
    ></div>
`,
})
export class HeroAsyncMessageComponent {
  b: string = ""
}
