// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, OnInit} from '@angular/core';

@Component({
   selector: 'app-root',
   template: `
    <app-root [<usage>f<caret>oo</usage>]="12">{{ <usage>foo</usage> }}</app-root>
    <app-root <usage>foo</usage>="12"></app-root>
   `,
   inputs: [ "<usage>foo</usage>"],
 })
export class AppComponent implements OnInit {
  <usage>foo</usage> : 12
}
