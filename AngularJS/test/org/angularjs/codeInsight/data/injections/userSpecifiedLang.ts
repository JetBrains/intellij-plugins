import {Component} from "angular2/core";

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