// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core";

@Component({
    selector:'todo-list',
    template:`<div></div>`,
    styles: [/* language=SCSS */ `
        $text-color: #555555;
        body {
            color: $text-color;
        }
    `,`
        body {
            color: #00aa00;
        }
    `]
})
export class TodoList{
}
