import {Component, View} from 'angular2/core';
import {EventEmitter} from "events";
import {Input, Output} from "angular2/core";
import {Customer} from "./customer";

@Component({
    selector: 'todo-cmp',
    template: `<div (click)="myCustomer.ca<caret>ll()"></div>`
})
export class TodoCmp {
    private myCustomer:Customer;

    onCompletedButton() {
        this.other = [1, 2, 3];
    }
}
