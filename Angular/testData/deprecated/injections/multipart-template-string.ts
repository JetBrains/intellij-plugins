// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

/* tslint:disable:max-line-length */
import {Component} from '@angular/core';

const test = 'foo';

@Component({
  selector: 'app-root',
  template: `
    <div style="text-align:center">
      <h1>
        Welcome to {{ title }}!
      </h1>
    </div>
    <div>
      {{ foo.${test} }}
      {{ title<error descr="Unexpected token '12'"> </error>${12} }}
    </div>
    <a>
  <error descr="Element a is not closed">`</error>,
  styleUrls: ['./foo-bar.component.css']
})
export class FooBarComponent {
  title = 'untitled16';
}
