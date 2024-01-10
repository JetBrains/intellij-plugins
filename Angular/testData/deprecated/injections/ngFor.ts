// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input} from "@angular/core";
import {TodoItemRenderer} from "./todo-item-renderer";
import {StartedPipe} from "./started-pipe";
import {SearchPipe} from "./search-pipe";

@Component({
    selector:'todo-list',
    pipes: [StartedPipe, SearchPipe],
    directives: [TodoItemRenderer],
    template:`<div>
        <ul>
            <li *ngFor="let myTodo of todoService.todos
            | started : status
            | search : term
            ">
                <todo-item-renderer
                [todo]="myT<caret>"
                (toggle)="todoService.toggleTodo($event)"
                ></todo-item-renderer>
            </li>
        </ul>
    </div>`
})
export class TodoList{
    @Input() status;
    @Input() term;
    constructor(public todoService:TodoService){}
}

export class TodoService {
    todos: string[]
}
