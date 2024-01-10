// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, EventEmitter, Output} from '@angular/core';

@Component({
             standalone: true,
             selector: 'app-root',
             template: `<div (animationend)="emitter.emit($event)"></div>`
           })
export class AppComponent {
    @Output() emitter = new EventEmitter<AnimationEvent>();<caret>
}
