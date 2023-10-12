// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Input} from '@angular/core';

@Component({
             inputs: ["test1:alias1"],
             selector: 'app-test',
             standalone: true,
             template: `
      <div [title]="test1 | as<caret>ync "></div>
    `
           })
export class TestComponent {
  @Input() test1!: number;
}