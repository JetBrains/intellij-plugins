import {Component} from "@angular/core"

@Component({
    selector: 'todo-cmp',
    template: `<todo-cmp (event)="fetchFromApi('')"</todo-cmp>`,
})
export class TodoCmp {
    fetchFromApi(s: string) {

    }
}
