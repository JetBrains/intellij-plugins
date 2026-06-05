// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';

@Component({
  selector: 'my-comp',
  template: `
  {{ () => <error descr="Multi-line arrow functions are not supported. If you meant to return an object literal, wrap it with parentheses.">{<weak_warning descr="TS7028: Unused label.">a</weak_warning>: 12}</error> }}
  {{ () => <error descr="Multi-line arrow functions are not supported. If you meant to return an object literal, wrap it with parentheses.">{}</error> }}
  {{ () => <error descr="Multi-line arrow functions are not supported. If you meant to return an object literal, wrap it with parentheses.">{<error descr="TS2339: Property 'return' does not exist on type 'MyComponent'.">return</error><error descr="Newline or semicolon expected"> </error><error descr="TS1005: ';' expected.">12</error>}</error> }}
  `,
})
export class MyComponent {

}