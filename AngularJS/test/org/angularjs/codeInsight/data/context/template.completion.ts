import {Component, View} from 'angular2/core';
import {EventEmitter} from "events";
import {Input, Output} from "angular2/core";

@Component({
    selector: 'todo-cmp',
})
export class TodoCmp {
    private myCustomer:string[];
    onCompletedButton() {
        this.other = [1, 2, 3];
    }
}
