// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import {Component, Directive, EventEmitter, Output} from "@angular/core"

export interface Item {
  someName: string;
  someValue: number;
}

export abstract class BaseComponent<T> {
  items: T[];
  @Output()
  event: EventEmitter<T>;
}

@Directive({
  selector: "[event]"
})

@Component({
  templateUrl: "./genericParentClassMembers.html"
})
export class AppComponent extends BaseComponent<Item> {

  fun(event: string) {

  }
}
