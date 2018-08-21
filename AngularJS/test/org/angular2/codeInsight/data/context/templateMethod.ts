// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input, Output, View} from 'angular2/core';
import {EventEmitter} from "events";
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
