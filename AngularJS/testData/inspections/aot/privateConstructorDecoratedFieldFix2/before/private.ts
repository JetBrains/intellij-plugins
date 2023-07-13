// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, SkipSelf} from "@angular/core"

@Component({
  templateUrl: "./private.html"
})
export class MyComponent {

  constructor(@SkipSelf() private privateField: string) {

  }

}
