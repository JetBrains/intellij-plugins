import {Component, View} from 'angular2/core';
import {EventEmitter} from "events";
import {Input, Output} from "angular2/core";

@Component({
    selector: 'todo-cmp',
    styles: [`
  div {
    font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  }
`],
    template: `<div #myDiv (click)="oCBu<caret>" [todo]="a"></div>`,
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
