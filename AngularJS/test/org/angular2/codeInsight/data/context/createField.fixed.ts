import {Component} from "@angular/core"

@Component({
    selector: 'todo-cmp',
    templateUrl: "./createField.html",
    template: `<div>{{todo}}</div>`,
})
export class TodoCmp {
    todo: any;
}
