// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input} from '@angular/core';


@Component({
    selector: 'null-checks',
    templateUrl: './NullChecks.html'
})
export class TestComponent {
  bar: string | undefined = (() => undefined)();

  @Input foo: string;

  acceptString(x: string): string {
    return x;
  }
}
