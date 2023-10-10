// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Input} from '@angular/core';

@Component({
   selector: 'app-test',
   standalone: true,
   template: ``
 })
export class TestComponent {
  @Input() test1!: boolean;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    TestComponent
  ],
  template: `
    <app-test test1="t<caret>rue"/>
   `
})
export class AppComponent {
}