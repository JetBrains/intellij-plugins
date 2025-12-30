// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  template: `
      <div>{{ 'foo' <error descr="The in operator is supported only in Angular 20 and above.">in</error> data }}</div>
      @if (key <error descr="The in operator is supported only in Angular 20 and above.">in</error> {foo: 'bar'}) {
        has {{key}}
      } @else {
        no {{key}}
      }
      {{ data.in }}
  `,
  standalone: true
})
export class AppComponent {
  data!: { foo: 12, in: 12 };
  key: string | number = 'foo';

  constructor() {
  }

}