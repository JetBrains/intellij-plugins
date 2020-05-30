import {Component} from "@angular/core"

@Component({
    selector: 'todo-cmp',
    template: `<todo-cmp (event)="fetch<caret>FromApi($event)"</todo-cmp>`,
})
export class TodoCmp {
    @Output()
    event: EventEmitter<Bar>;
}

class Bar {
    foo() {}
}
