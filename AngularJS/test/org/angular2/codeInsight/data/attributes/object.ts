// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';
import {EventEmitter} from "events";

@Component({selector: 'todo-cmp,[todo-cmp]'})
export class TodoCmp {
    @Input() model: Object;
    @Input() oneTime: string;
    @Input() oneTimeList: FloatPlaceholderType;
    @Input() id: string;
    @Output() complete = new EventEmitter(); // TypeScript supports initializing fields

    onCompletedButton() {
        this.complete.emit("completed"); // this fires an event
    }
}

export type FloatPlaceholderType = 'always' | 'never' | 'auto';
