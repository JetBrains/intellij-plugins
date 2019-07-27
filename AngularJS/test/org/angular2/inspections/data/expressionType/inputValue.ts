// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

interface Foo {
  foo(): string
}

@Component({
  selector: "foo",
  templateUrl: "./inputValue.html"
})
export class Comp {

  onClick(val: string) {

  }

  onClick2(val2: Foo) {

  }
}
