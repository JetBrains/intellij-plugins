// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
// noinspection TypeScriptCheckImport
import {Component} from '@angular/core';

@Component({
  template: `
    {{items[0].myBar}}
    {{items[0].myBars}}
    {{item.boo2}}
    {{item.fooBar}}
  `
})
export class AppComponent {

  protected item: any;
  protected items: any[] = [{myBar: 'test', fooBar: 12}];

  test() {
    // noinspection JSUnusedLocalSymbols
    let a = this.items[0].myBar;
    // noinspection JSUnusedLocalSymbols
    let b = this.items[0].myBark;
    this.item.boo2;
    this.item.fooBar;
  }
}
