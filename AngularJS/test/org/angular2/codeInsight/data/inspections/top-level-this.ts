// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import {Component} from "@angular/core"

@Component({
  templateUrl: "./top-level-this.html"
})
export class MyComponent {

  got_a_click() {

  }

  get_title(): string {
    return null;
  }
}
