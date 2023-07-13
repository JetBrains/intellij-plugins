// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

@Component({
  template: `
    <div #ref1></div>
    <ng-template>
      <div #ref2></div>
    </ng-template>
    {{ this.<caret> }}
  `
})
export class MyComponent {

  title: string

  getSomething(): number {
    return 0;
  }

}
