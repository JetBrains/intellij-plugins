// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import {Component} from "@angular/core"
import {NgIf} from '@angular/common';

@Component({
  selector: "foo",
  template: `
    <ng-template #foo let-modal
                 *ngIf="test as t; else: bar">
      <p>{{t('text1')}}</p>
    </ng-template>
    <ng-template #bar>
    </ng-template>
 `,
  imports: [NgIf]
})
export class MyComp2 {
  test: boolean
}
