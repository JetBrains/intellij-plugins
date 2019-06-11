// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {NgModule} from '@angular/core';
import {MyTestLibComponent} from './my-test-lib.component';
import {SecondComponentComponent} from './second-component.component';

export function fun1(test: boolean) {
  return [
    MyTestLibComponent,
    SecondComponentComponent
  ];
}

export function fun2(test: boolean): any[] {
  return [
    test ? [MyTestLibComponent] : [SecondComponentComponent]
  ];
}


@NgModule({
  declarations: fun1(false),
  imports: [
  ],
  exports: [fun2(true)]
})
export class MyTestLibModule { }
