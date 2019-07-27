// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive, Input, Output} from '@angular/core';

@Component({
    selector: 'hero-message',
    template: `
        <div <caret>></div>
        <div myPlain="foo" [myPlain]="foo" (myPlain)="foo" [(myPlain)]="foo"
             myInput="foo" [myInput]="foo" (myInput)="foo" [(myInput)]="foo"  
             mySimpleBindingInput="foo" [mySimpleBindingInput]="foo" (mySimpleBindingInput)="foo" [(mySimpleBindingInput)]="foo"
             myOutput="foo" [myOutput]="foo" (myOutput)="foo" [(myOutput)]="foo"
             myInOut="foo" [myInOut]="foo" (myInOut)="foo" [(myInOut)]="foo"
             myInOutChange="foo" [myInOutChange]="foo" (myInOutChange)="foo" [(myInOutChange)]="foo"
             fake="foo" [fake]="foo" (fake)="foo" [(fake)]="foo"
             fakeChange="foo" [fakeChange]="foo" (fakeChange)="foo" [(fakeChange)]="foo"
        ></div>
`,
})
export class HeroAsyncMessageComponent {
    delayedMessage:Promise<string> = new Promise((resolve, reject) => {
        setTimeout(() => resolve('You are my Hero!'), 500);
    });
}

@Directive({
    selector: '[myInput],[mySimpleBindingInput],[myOutput],[myInOut],[myPlain],[fake],[fakeChange]',
    inputs: ['fake'],
    outputs: ['fakeChange']
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
