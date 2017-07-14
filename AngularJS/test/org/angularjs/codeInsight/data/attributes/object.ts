import {Component, View} from 'angular2/angular2';
import {EventEmitter} from "events";

@Component({selector: 'todo-cmp,[todo-cmp]'})
class TodoCmp {
    @Input() model: object;
    @Input() oneTime: string;
    @Input() oneTimeList: FloatPlaceholderType;
    @Input() id: string;
    @Output() complete = new EventEmitter(); // TypeScript supports initializing fields

    onCompletedButton() {
        this.complete.emit("completed"); // this fires an event
    }
}

export type FloatPlaceholderType = 'always' | 'never' | 'auto';
