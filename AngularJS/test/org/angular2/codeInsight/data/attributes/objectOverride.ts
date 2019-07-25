// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';
import {EventEmitter} from "events";

@Component({selector: 'todo-cmp'})
class TodoCmp {
    @Input("model") something;
    @Output("complete") else = new EventEmitter(); // TypeScript supports initializing fields

    onCompletedButton() {
        this.complete.emit("completed"); // this fires an event
    }
}
