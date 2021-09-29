// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, QueryList} from "@angular/core"

@Component({
  template: `
    <div *ngFor="let item of <error descr="Type 12 is not assignable to type NgIterable<T> | QueryList<T>">12</error>">

    </div>
    <div *ngFor="let item of query">
      {{item.bold()}}
      {{item.charAt(12)}}
      {{item.charAt(<error descr="Argument type \"str\" is not assignable to parameter type number">"str"</error>)}}
      {{item.<error descr="Unresolved variable foo">foo</error>}}
    </div>
    <div *ngFor="let item of <error descr="Type 15 is not assignable to type NgIterable<T> | QueryList<T>">15</error>">

    </div>
  `
})
export class MyComponent {

  query: QueryList<string>

}
