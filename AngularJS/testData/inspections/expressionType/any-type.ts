// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
// noinspection TypeScriptCheckImport
import {Component} from '@angular/core';

@Component({
  template: `
    {{items[0].myBar}}
    {{items[0].<weak_warning descr="Unresolved variable myBars">myBars</weak_warning>}}
    {{item.<weak_warning descr="Unresolved variable boo2">boo2</weak_warning>}}
    {{item.fooBar}}
  `
})
export class AppComponent {

  private item: any;
  private items: any[] = [{myBar: 'test', fooBar: 12}];

  test() {
    // noinspection JSUnusedLocalSymbols
    let a = this.items[0].myBar;
    // noinspection JSUnusedLocalSymbols
    let b = this.items[0].<weak_warning descr="Unresolved variable myBark">myBark</weak_warning>;
    this.item.<weak_warning descr="Unresolved variable boo2">boo2</weak_warning>;
    this.item.fooBar;
  }
}
