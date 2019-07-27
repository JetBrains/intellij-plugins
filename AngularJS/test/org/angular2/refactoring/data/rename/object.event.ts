// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';
import {EventEmitter} from "events";

@Component({selector: 'todo-cmp'})
export class TodoCmp {
    @Input() model;
    @Output() complete2 = new EventEmitter(); // TypeScript supports initializing fields

    onCompletedButton() {
        this.complete2.emit("completed"); // this fires an event
    }
}

