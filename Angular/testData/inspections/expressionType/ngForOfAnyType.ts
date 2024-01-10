// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

@Component({
  template:`
    <div *ngFor='let item of foo'>
      {{item.<weak_warning descr="Unresolved variable blibop">blibop</weak_warning>}}
    </div>
    <div *ngFor='let item of $any(foo)'>
      {{item.<weak_warning descr="Unresolved variable blibop">blibop</weak_warning>}}
    </div>
    <div *ngFor='let item of $any(foo) | async'>
      {{item.blibip}}
    </div>
  `
})
export class MyComponent {
    foo: any = [{blibip: 12}]
}
