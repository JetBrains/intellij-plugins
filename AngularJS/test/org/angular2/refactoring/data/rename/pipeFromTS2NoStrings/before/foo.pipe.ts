// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
    name: 'f<caret>oo'
})
export class FooPipe implements PipeTransform {

    transform(a: number): string {
        return '12' + a;
    }

}
