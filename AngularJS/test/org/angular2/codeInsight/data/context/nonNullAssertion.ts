// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';
import {Customer} from "./customer";

@Component({
    selector: 'todo-cmp',
    templateUrl: "./nonNullAssertion.html",
})
export class TodoCmp {
    private myCustomer: Customer | null;

    onCompletedButton() {
        this.other = [1, 2, 3];
    }
}
