import {Component} from "@angular/core"

@Component({
    selector: 'todo-cmp',
    template: `<div>{{fetchFromApi(2, "a", foobarbaz)}}</div>`,
})
export class TodoCmp {
    fetchFromApi(number: number, a: string, foobarbaz: any) {

    }
}
