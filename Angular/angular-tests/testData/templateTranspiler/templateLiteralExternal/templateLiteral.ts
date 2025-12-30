// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from '@angular/core';
import {DatePipe, UpperCasePipe} from '@angular/common';

@Component({
  selector: 'app-root',
  templateUrl: "./templateLiteral.html",
  imports: [DatePipe, UpperCasePipe],
  standalone: true,
})
export class AppComponent {
}
