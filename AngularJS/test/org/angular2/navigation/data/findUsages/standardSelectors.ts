// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Directive} from "@angular/core";

@Directive({
  selector: "div,[class]",
  template: `<div class="foo"></div>`
})
export class TestDirective {

  constructor() {
  }

}
