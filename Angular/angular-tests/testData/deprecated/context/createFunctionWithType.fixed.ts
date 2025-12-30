import {Component} from "@angular/core"

@Component({
    selector: 'todo-cmp',
    template: `<todo-cmp (event)="fetchFromApi('')"</todo-cmp>`,
})
export class TodoCmp {
    protected fetchFromApi(s: string) {

    }
}
