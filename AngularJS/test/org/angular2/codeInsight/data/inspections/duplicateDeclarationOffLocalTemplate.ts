// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  template: `
    <div *ngFor='let item of items'>{{item}}</div>
    <div *ngFor='let item of items'>{{item}}</div>
  `,
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  items: [];
}
