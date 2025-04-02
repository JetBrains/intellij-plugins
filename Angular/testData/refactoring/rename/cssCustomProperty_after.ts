// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <div [style.--foo]="'#ff0000'">
      <span style="color: var(--fo<caret>o)">Test</span>
    </div>
    <span style="color: var(--test2)">Test2</span>
  `
})
export class AppComponent {
}
