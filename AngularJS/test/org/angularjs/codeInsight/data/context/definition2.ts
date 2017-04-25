import {Component, View} from 'angular2/core';
import {EventEmitter} from "events";
import {Input, Output} from "angular2/core";

@Component({
    selector: 'todo-cmp',
    template: `<div>{{checker.check<caret>ed = 1}}</div>`,
})
export class TodoCmp {
    title:string;
    checker:Checker;
}

class Checker {
    checked:boolean;
}
