// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from '@angular/core';
import {NgIf} from "@angular/common";

@Component({
             selector: 'test',
             standalone: true,
             imports: [NgIf],
             template: `
    <div *ngIf="x as y" #div>
     <button (click)="<error descr="Attempt to assign to const or readonly variable">y</error> = true; <error descr="Attempt to assign to const or readonly variable">div</error> = null">Toggle</button>
    </div>
    <ng-template [ngIf]="x" let-y="x">
      <button (click)="<error descr="Attempt to assign to const or readonly variable">y</error> = true">Toggle</button>
    </ng-template>
  `,
           })
export class TestComponentOne {
  x!: boolean;
}
