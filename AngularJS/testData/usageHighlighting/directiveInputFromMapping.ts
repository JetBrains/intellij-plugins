// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from '@angular/core';

@Component({
   selector: 'app-root',
   template: `
    <app-root [<usage>foo</usage>]="12">{{ <usage>foo</usage> }}</app-root>
    <app-root <usage>foo</usage>="12"></app-root>
   `,
   inputs: [ "<usage>f<caret>oo</usage>"],
 })
export class BoldDirective {
  <usage>foo</usage> = "12"
}
