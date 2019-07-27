import {Component} from "@angular/core"

@Component({
    selector: 'todo-cmp',
    templateUrl: "./createFunction.html",
    template: `<div>{{fetch<caret>FromApi()}}</div>`,
})
export class TodoCmp {
}
