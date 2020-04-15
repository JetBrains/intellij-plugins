// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NgxsModule } from '@ngxs/store';

import { AmountComponent } from './amount.component';
import { AmountState } from './amount.state';
import { PriceState } from './price.state';

@NgModule({
  declarations: [AmountComponent],
  imports: [
    NgxsModule.forFeature([AmountState, PriceState])
  ]
})
export class AmountModule {}
