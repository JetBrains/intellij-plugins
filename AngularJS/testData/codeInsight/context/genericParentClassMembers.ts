// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import {Component, Directive, EventEmitter, Input, Output, Pipe} from "@angular/core"

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
  selector: "[event],[foo]"
})
export class MyDirective extends BaseComponent<Item>{
  @Input()
  foo: string
}
@Component({
  templateUrl: "./genericParentClassMembers.html"
})
export class AppComponent extends BaseComponent<Item> {

  fun(event: string) {

  }
}

@Pipe({
  name: "pipe"
})
export class MyPipe {

  transform(val: string): string;
  transform(val: number): number;
  transform(val: string | number) : string | number {
    return val;
  }

}
