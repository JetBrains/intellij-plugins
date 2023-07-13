// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';

@Component({
    selector: 'app-root',
    template:`<div style="text-align:center">
      <h1><error descr="TSLint: Each element containing text node should have an i18n attribute (template-i18n)">Welcome</error> to {{ title }}!</h1>
  </div>`,
    styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = <error descr="TSLint: &quot; should be ' (quotemark)">"ng-cli-codelyzer"</error>;
}
