import {Component, View} from 'angular2/angular2';
import {EventEmitter} from "events";

@Component({selector: 'todo-cmp'})
class TodoCmp {
    @Input() model;
    @Input() oneTime: string;
    @Output() complete = new EventEmitter(); // TypeScript supports initializing fields

    onCompletedButton() {
        this.complete.emit("completed"); // this fires an event
    }
}
