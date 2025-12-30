// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, EventEmitter, Input, Output} from '@angular/core';

@Component({
    templateUrl: './generics.html'
})
export class GenericsComponent {

    useNumber(val: number) {

    }

    useString(val: string) {

    }

}

@Directive({
    selector: "[gen]"
})
export class GenericsDirective<T, S> {

    @Input()
    input1: T;

    @Input()
    input2: T;

    @Input()
    input3: S;

    @Output()
    output1: EventEmitter<T>

    @Output()
    output3: EventEmitter<S>

}

@Directive({
    selector: "[gen2]"
})
export class GenericsDirective2<T> {

    @Input()
    input1: T;

    @Input()
    input2: string;

}