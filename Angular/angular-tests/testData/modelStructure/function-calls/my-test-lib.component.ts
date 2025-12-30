// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'lib-my-test-lib',
  template: `
    <p>
      my-test-lib works!
    </p>
  `,
  styles: []
})
export class MyTestLibComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}
