// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input, Output} from '@angular/core';
import {EventEmitter} from "events";

@Component({
    selector: 'todo-cmp',
    template: `
  <div #myDiv (click)="onCompletedButton()" [todo]="myDiv">
    <div [ngClass]="{'current':i == (wordIndex | async)}">{{todo}}</div>
    <todo-cmp (click)="deleteTodo" [todo]="!myInput?.isValid" (window:resize)="">aaa</todo-cmp>
    <div *ngFor="let something of items" class="inDa<caret>Class foo2">
    {{something.}}
</div>
  </div>`})
export class TodoCmp {
    items:string[];
    private other:string[];
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
