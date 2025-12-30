// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input, Output} from '@angular/core';
import {EventEmitter} from "events";

@Component({
    selector: 'todo-cmp',
    styles: [`
  div {
    font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  }
`],
    template: `<div #myDiv (click)="onCompletedButton()<caret>" [todo]="a"></div>`,
    templateUrl: 'playground.html',
    styleUrls: ['playground.css'

    ]
})
export class TodoCmp {
    @Input()
    todo;
    @Output()
    toggleTodo = new EventEmitter();
    @Output()
    deleteTodo = new EventEmitter();

    onCompletedButton() {
        this.deleteTodo.emit("completed"); // this fires an event
    }
}
