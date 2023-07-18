// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Directive} from "@angular/core";

@Directive({
  selector: "di<caret>v,[class]",
  template: `<div class="foo"></div>`
})
export class TestDirective {
}
