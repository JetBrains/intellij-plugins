// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {<info descr="identifiers//exported function">Component</info>} <info descr="null">from</info> '@angular/core';

<info descr="decorator">@</info><info descr="decorator">Component</info>({
   <info descr="instance field">selector</info>: 'robot-profile',
   <info descr="instance field">standalone</info>: true,
   <info descr="instance field">template</info>: `<inject descr="null">
      @for (<info descr="ng-variable">foo</info> <info descr="null">of</info> <info descr="instance field">bar</info>; track <info descr="ng-variable">foo</info>) {
      }
    </inject>`
 })
export class <info descr="classes//exported class">RobotProfileComponent</info> {
  <info descr="instance field">bar</info>: <info descr="null">string</info>[]
}
