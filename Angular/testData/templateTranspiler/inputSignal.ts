// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import {Component, Input, input} from "@angular/core"

@Component({
   selector: 'foo',
   standalone: true,
   template: `
    <foo
      [bar]="2"
      [boo]="3"
      #bar>
    </foo>
    {{ bar }}
  `,
 })
export class MyComp2<T> {
  @Input() bar!: T;

  boo = input<T>();
}
