// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {<symbolName descr="identifiers//exported function">Component</symbolName>} <info descr="null">from</info> '@angular/core';

<info descr="decorator">@</info><info descr="decorator">Component</info>({
   <symbolName descr="instance field">selector</symbolName>: '<symbolName descr="HTML_TAG_NAME">robot-profile</symbolName>',
   <symbolName descr="instance field">standalone</symbolName>: true,
   <symbolName descr="instance field">template</symbolName>: `<inject descr="null">
      @for (<symbolName descr="ng-variable">foo</symbolName> <info descr="null">of</info> <symbolName descr="instance field">bar</symbolName>; track <symbolName descr="ng-variable">foo</symbolName>) {
      }
    </inject>`
 })
export class <symbolName descr="classes//exported class">RobotProfileComponent</symbolName> {
  <symbolName descr="instance field">bar</symbolName>!: <info descr="null">string</info>[]
}
