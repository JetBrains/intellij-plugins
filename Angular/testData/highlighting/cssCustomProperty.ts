// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <div [style.--test2]="'#ff0000'">
      <span style="color: var(--test2)">Test</span>
    </div>
    <span style="color: var(<error descr="Cannot resolve '--test2' custom property">--test2</error>)">Test2</span>
    <span style="color: var(--test3)">Test3</span>
  `,
  host: {
    "[style.--test3]": "'#ffd000'",
    "style": "color: var(--test3)"
  }
})
export class AppComponent {}
