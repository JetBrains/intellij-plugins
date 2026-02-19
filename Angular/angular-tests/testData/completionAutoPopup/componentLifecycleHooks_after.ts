// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, OnInit, SimpleChanges} from '@angular/core';

@Component({
             selector: 'app-root',
             template: ``,
             styleUrls: ['./app.component.css']
           })
export class AppComponent {
  ngOnChanges(changes: SimpleChanges<AppComponent>) {<caret>
  }
}