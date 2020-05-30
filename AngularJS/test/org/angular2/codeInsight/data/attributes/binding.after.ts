// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';
// Initial view: "Message: "
// After 500ms: Message: You are my Hero!"
@Component({
    selector: 'hero-message',
    template: `
        <input type="text" #input_el (keyup)="0">
        <button [disabled]="input_el<caret>.value === 'yes'">disable me</button>
`,
})
export class HeroAsyncMessageComponent {
    delayedMessage:Promise<string> = new Promise((resolve, reject) => {
        setTimeout(() => resolve('You are my Hero!'), 500);
    });
}
