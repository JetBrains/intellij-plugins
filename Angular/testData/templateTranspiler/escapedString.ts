// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input} from "@angular/core";

@Directive({
   standalone: true,
   selector: "[color]"
 })
export class MyDirective<C,F> {
  /**
   * The color input
   **/
  @Input("color")
  myInput: C
}

@Component({
   standalone: true,
   template: "<div [color]='\"red\"'></div>",
   imports: [
     MyDirective
   ]
 })
class MyComponent {
}