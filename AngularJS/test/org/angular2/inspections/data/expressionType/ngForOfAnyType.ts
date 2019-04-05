// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

@Component({
  template:`
    <div *ngFor='let item of $any(<error descr="Unresolved variable or type foo">foo</error>)'>
      {{item.<weak_warning descr="Unresolved variable blibop">blibop</weak_warning>}}
    </div>
    <div *ngFor='let item of $any(<error descr="Unresolved variable or type foo">foo</error>) | async'>
      {{item.<weak_warning descr="Unresolved variable blibip">blibip</weak_warning>}}
    </div>
  `
})
export class MyComponent {

}
