// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, NgModule} from "@angular/core"
import {CommonModule} from "@angular/common"

@Component({
  template:`
    <div *ngFor='let item of foo'>
      {{item.blibop}}
    </div>
    <div *ngFor='let item of $any(foo)'>
      {{item.blibop}}
    </div>
    <div *ngFor='let item of $any(foo) | async'>
      {{item.blibip}}
    </div>
  `
})
export class MyComponent {
    foo: any = [{blibip: 12}]
}

@NgModule({
  declarations: [MyComponent],
  imports: [CommonModule]
})
export class NgForOfAnyTypeStrictModule {

}
