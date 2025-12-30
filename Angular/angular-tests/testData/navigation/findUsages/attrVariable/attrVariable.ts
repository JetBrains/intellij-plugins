// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, ElementRef, ViewChild} from '@angular/core';

@Component({
             selector: 'app-root',
             templateUrl: './attrVariable.html'
           })
export class AttrVariable {
  title = 'untitled15';
  @ViewChild('someText') someText!: ElementRef<HTMLElement>;
}
