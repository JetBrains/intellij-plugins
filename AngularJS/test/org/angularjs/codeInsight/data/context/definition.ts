import {Component, View} from 'angular2/core';
import {EventEmitter} from "events";
import {Input, Output} from "angular2/core";

@Component({
    selector: 'todo-cmp',
    template: `<div>{{tit<caret>le = 1}}</div>`,
})
export class TodoCmp {
    title:string;
}
