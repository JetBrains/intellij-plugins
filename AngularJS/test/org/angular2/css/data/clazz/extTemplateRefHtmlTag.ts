// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input, Output} from '@angular/core';
import {EventEmitter} from "events";

@Component({
    selector: 'todo-cmp',
    templateUrl: "./extTemplateRefHtmlTag.html",
    styles: [`.inDaClass {
    }`]
})
export class TodoCmp {
    items: string[];
    private other: string[];
    @Input()
    todo;
    @Output()
    toggleTodo = new EventEmitter();
    @Output()
    private deleteTodo = new EventEmitter();

    onCompletedButton() {
        this.deleteTodo.emit("completed"); // this fires an event
        this.other = [1, 2, 3];
    }
}
