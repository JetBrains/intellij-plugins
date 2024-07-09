// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input} from '@angular/core';
import {NgModel} from '@angular/forms';

export declare type MyType = 'off' | 'polite' | 'assertive';

@Component({
    selector: 'my-comp',
    template: `
      <div></div>`,
    inputs: [
        'id:dependency'
    ],
})
export class ComponentWithTypes {
    @Input()
    plainBoolean: boolean;

    @Input()
    plainNumber: number;

    @Input()
    simpleStringEnum: MyType;

    @Input()
    element: HTMLElement;

    @Input()
    model: NgModel;

    @Input
    obj: { f: string, e: MyType }

    @Input()
    set setterSimpleStringEnum(value: MyType) {

    }

    @Input()
    predicate(arg: string) {
        return false;
    }

    id: string;
}
