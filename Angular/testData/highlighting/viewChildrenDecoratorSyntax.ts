// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {<symbolName descr="identifiers//exported function">Component</symbolName>, <symbolName descr="classes//exported class">ElementRef</symbolName>, <symbolName descr="identifiers//exported function">ViewChildren</symbolName>, <symbolName descr="identifiers//exported function">ViewChild</symbolName>} <info descr="null">from</info> '@angular/core';

<symbolName descr="decorator">@</symbolName><symbolName descr="decorator">Component</symbolName>({
    <symbolName descr="instance field">selector</symbolName>: '<symbolName descr="HTML_TAG_NAME">app-test</symbolName>',
    <symbolName descr="instance field">template</symbolName>: `<inject descr="null">
      <textarea #<symbolName descr="ng-variable">area</symbolName>></textarea>
      <textarea #<symbolName descr="ng-variable">area</symbolName>></textarea>
      <div #<symbolName descr="ng-variable">area2</symbolName>></div>
      <div #<symbolName descr="ng-variable">area2</symbolName>></div>
    </inject>`
})
export class <symbolName descr="classes//exported class">TestComponent</symbolName> {
  <symbolName descr="decorator">@</symbolName><symbolName descr="decorator">ViewChildren</symbolName>('<symbolName descr="NG.VARIABLE">area2</symbolName>') <symbolName descr="instance field">area</symbolName>!: <symbolName descr="classes//exported class">ElementRef</symbolName>;
  <symbolName descr="decorator">@</symbolName><symbolName descr="decorator">ViewChild</symbolName>('<symbolName descr="NG.VARIABLE">area</symbolName>') <symbolName descr="instance field">area2</symbolName>!: <symbolName descr="classes//exported class">ElementRef</symbolName>;
}
