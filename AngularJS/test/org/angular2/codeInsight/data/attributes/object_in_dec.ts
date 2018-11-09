// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, View} from 'angular2/angular2';
import {EventEmitter} from "events";

@Component({selector: 'todo-cmp,[todo-cmp]', inputs: ["modelek:model", "id"], outputs: ["complete_: complete"]})
export class TodoCmp {
    modelek: Object;
    @Input() oneTime: string;
    @Input() oneTimeList: FloatPlaceholderType;
    id: string;
    complete_ = new EventEmitter(); // TypeScript supports initializing fields

    onCompletedButton() {
        this.complete_.emit("completed"); // this fires an event
    }
}

export type FloatPlaceholderType = 'always' | 'never' | 'auto';
