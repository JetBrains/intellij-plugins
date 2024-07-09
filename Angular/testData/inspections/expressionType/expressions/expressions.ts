// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';
import {NgModel} from '@angular/forms';
import {Observable} from 'rxjs';


@Component({
    selector: 'expr',
    templateUrl: './expressions.html'
})
export class ExpressionsComponent {

    asyncString(): Observable<string> {
        return null;
    }

    asyncModel(): Observable<NgModel> {
        return null;
    }

    getText(model: NgModel): string {
        return null;
    }

    foo(arg: string): boolean {
        return true;
    }

    bar(arg: number): boolean {
        return false;
    }

    async asyncInferred() {

    }

    async asyncInferredString() {
        return "string";
    }

}
