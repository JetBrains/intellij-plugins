// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  template: `
      <div>{{ 'foo' in data }}</div>
      @if (key in {foo: 'bar'}) {
        has {{key}}
      } @else {
        no {{key}}
      }
      {{ data.in }}
  `,
  standalone: true
})
export class AppComponent {
  data!: { foo: 12, in: 45 };
  key: string | number = 'foo';

  constructor() {
  }

}