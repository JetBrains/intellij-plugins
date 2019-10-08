// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

@Component({
  selector: `test`,
  template: `
    <ng-content select="foo,[bar]"></ng-content>
    <ng-content select=bar[foo]></ng-content>
    <ng-content select=":not([goo])"></ng-content>
  `
})
export class MyComponent {

}
