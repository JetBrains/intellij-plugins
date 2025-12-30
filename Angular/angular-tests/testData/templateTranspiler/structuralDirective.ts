// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Component, Input } from '@angular/core';
import { NgForOf, NgIf } from '@angular/common';

interface IData {
  icon?: string
  check?(): string
}

@Component({
  selector: 'app-root',
  imports: [NgForOf, NgIf],
  template: `
      <div *ngFor="let foo of bar; trackBy track; let index = index"></div>
      <div *ngIf="value as foo; else template as bar; let value = ngIf">
        <a (click)="use(foo)">test</a>
      </div>
  `,
  standalone: true
})
export class AppComponent {

  value: boolean
  use (v: boolean) {

  }
}