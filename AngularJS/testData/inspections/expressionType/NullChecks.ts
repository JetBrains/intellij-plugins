// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input} from '@angular/core';
import {CommonModule} from "@angular/common";

@Component({
  selector: 'null-checks',
  imports: [CommonModule],
  standalone: true,
  templateUrl: './NullChecks.html'
})
export class TestComponent {
  @Input foo!: string;

  bar: string | undefined = (() => undefined)();

  bazPromise = Promise.resolve("hello");

  acceptString(x: string): string {
    return x;
  }
}
