// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input} from '@angular/core';
import {ɵd} from "./ng-zorro-antd"

@Component({
  selector: 'my-icon',
  template: ''
})
export class MyIconComponent extends ɵd {
  @Input()
  public myInput: string;
}
