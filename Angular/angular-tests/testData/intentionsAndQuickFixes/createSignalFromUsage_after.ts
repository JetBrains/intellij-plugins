// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {
    Component, signal,
} from '@angular/core';

@Component({
  selector:  "app-test",
  template:`
    {{ fooSig() }}
  `
})
export class TestComponent {
    protected readonly fooSig = signal<string | null>(null);<caret>

}