// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'foo'
})
export class FooPipe implements PipeTransform {

  transform(value: any, args?: any): any {
    return null;
  }

}
