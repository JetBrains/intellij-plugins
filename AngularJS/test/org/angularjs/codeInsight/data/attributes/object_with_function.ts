import {Component, View} from 'angular2/angular2';
import {EventEmitter} from "events";

@Component({selector: '[todo-cmp]'})
class TodoCmp {
    _model;
    @Output() complete = new EventEmitter(); // TypeScript supports initializing fields

    @Input()
    get model() {
        return this._model;
    }
}
