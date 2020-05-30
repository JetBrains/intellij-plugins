import {Component} from "@angular/core"

@Component({
    selector: 'todo-cmp',
    template: `<div>{{fetch<caret>FromApi(2, "a", foobarbaz)}}</div>`,
})
export class TodoCmp {
}
