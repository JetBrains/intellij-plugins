// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterOutlet} from '@angular/router';
import {CdkVirtualForOf} from "@angular/cdk/scrolling";

@Component({
       selector: 'app-root',
   standalone: true,
  imports: [CommonModule, RouterOutlet, CdkVirtualForOf],
template:  `
      <div class='left-side'>
    <div *cdkVirtualFor="let item    of items; let i = index">
  {{ item.b }}
                         </div>
   </div>
  `,
  styleUrl: './app.component.css'})
export class AppComponent {
title = 'untitled372';

        items = [{a: 12}, {a: 14}]
}
