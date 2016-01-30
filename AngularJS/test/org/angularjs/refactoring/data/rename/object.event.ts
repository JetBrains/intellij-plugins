import {Component, View} from 'angular2/angular2';
import {EventEmitter} from "events";

@Component({selector: 'todo-cmp'})
export class TodoCmp {
    @Input() model;
    @Output() complete2 = new EventEmitter(); // TypeScript supports initializing fields

    onCompletedButton() {
        this.complete2.emit("completed"); // this fires an event
    }
}

