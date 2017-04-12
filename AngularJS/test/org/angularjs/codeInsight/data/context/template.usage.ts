import {Component, View} from 'angular2/core';
import {EventEmitter} from "events";
import {Input, Output} from "angular2/core";

@Component({
    selector: 'todo-cmp',
})
export class TodoCmp {
    onCompletedButton() {
        this.other = [1, 2, 3];
    }
}

new TodoCmp