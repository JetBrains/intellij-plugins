// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, ElementRef, ViewChild} from '@angular/core';

@Component({
    selector: 'app-test',
    template: `
        <textarea #area></textarea>
        <div #myFoo></div>
        <div #area2></div>
        <div #area2></div>
        <div *ngIf="true" #area3></div>
        <div *ngIf="true" #area3></div>
    `
})
export class TestComponent {
    @ViewChild('myF<caret>oo') area: ElementRef;
}
