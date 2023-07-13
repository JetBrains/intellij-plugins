// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'standalone',
  standalone: true
})
export class StandalonePipe implements PipeTransform {

  transform(value: number, exponent = 1): number {
    return Math.pow(value, exponent);
  }

}
