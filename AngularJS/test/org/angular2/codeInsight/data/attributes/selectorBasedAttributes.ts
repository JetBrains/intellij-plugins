// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, Input, Output} from 'angular2/core';
// Initial view: "Message: "
// After 500ms: Message: You are my Hero!"
@Component({
    selector: 'hero-message',
    template: `
        <div <caret></div>
`,
})
export class HeroAsyncMessageComponent {
    delayedMessage:Promise<string> = new Promise((resolve, reject) => {
        setTimeout(() => resolve('You are my Hero!'), 500);
    });
}

@Directive({
    selector: '[myInput],[mySimpleBindingInput],[myOutput],[myInOut],[myPlain]'
})
export class TestDirective {
    @Input
    myInput;
    @Input
    mySimpleBindingInput: string;
    @Output
    myOutput;
    @Input
    myInOut;
    @Output
    myInOutChange;
}