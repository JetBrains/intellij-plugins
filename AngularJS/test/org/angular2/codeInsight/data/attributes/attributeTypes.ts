// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, Input, Output, View} from 'angular2/angular2';
import {EventEmitter} from "events";
import {RefDirective} from "./exportAs";

@Component({
    selector: 'my-comp',
    template: `<my-comp foo <caret>`,
})
export class HeroAsyncMessageComponent {

    @Input
    plainBoolean: boolean;

    @Output("my-event")
    myEvent: EventEmitter<MyEvent>;

    @Input
    simpleStringEnum: MyType;

    doIt(dir: RefDirective) {

    }
}

export declare type MyType = 'off' | 'polite' | 'assertive';

export interface MyEvent {

}

@Directive({
    selector: "[foo]"
})
export class FooDir {
    @Input
    fooInput: string
}

@Directive({
    selector: "[bar]"
})
export class BarDir {
    @Input
    bar: string
}