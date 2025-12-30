// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Component, Input } from '@angular/core';

interface IData {
  icon?: string
}

@Component({
             selector: 'app-root',
             template: `
      <div>
          <i title>data</i>
          <i class="{{ data.icon }}">data</i>
          <i class="i-{{ data.icon }}">data</i>
          <i class="i-{{ data.icon?.toString() }}">data</i>
          <i class="i-{{ data.icon || '' }}">data</i>
          <i [attr.class]="data.icon">data</i>
          <app-root <error descr="TS2322: Type 'string | undefined' is not assignable to type 'string'.
  Type 'undefined' is not assignable to type 'string'.">[foo]</error>="data.icon"></app-root>
      </div>
  `,
             standalone: true
           })
export class AppComponent {
  data: IData;

  constructor() {
    this.data = {}
  }

  @Input()
  protected foo!: string

}