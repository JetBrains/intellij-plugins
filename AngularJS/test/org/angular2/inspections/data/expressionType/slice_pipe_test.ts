// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

interface Foo {
  foo: string
}

// noinspection JSUnusedLocalSymbols
interface Bar {
  bar: string
}

@Component({
  template: `
    {{ (foo | slice : 0 : 1)[0].foo}}
    {{ (foo | slice : 0 : 1).entries()}}
    {{ (foo | slice : 0 : 1)[0].<error descr="Unresolved variable bar">bar</error>}}
    {{ (foo | slice : 0 : 1).<error descr="Unresolved variable bar1">bar1</error>}}
  `
})
export class MyComponent {
  foo: Foo[];
}
