// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Component, Input } from '@angular/core';

interface IData {
  icon?: string
  check?(): string
}

@Component({
 selector: 'app-root',
 template: `
      <div>
          <i class="i-{{ data?.icon?.toString() }}">data</i>
          <i class="i-{{ data?.check?.() }}">data</i>
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