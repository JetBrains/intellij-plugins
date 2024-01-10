// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {MyComponent} from "./component"

const obj = {
  component: MyComponent
}

export const schema = {
  declarations: [
    obj.component
  ],
  exports: [
    MyComponent
  ]
}
