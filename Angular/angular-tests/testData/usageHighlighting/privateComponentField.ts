// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core"

@Component({
  template: `
    {{ <usage>foo</usage> }}
  `
})
export class MyComponent {
  private <usage>f<caret>oo</usage>: string;

  private bar() {
    this.<usage>foo</usage> = "";
  }

}
