import {Component} from "@angular/core"

class EventEmitter<T> {}

@Component({
    selector: 'todo-cmp',
    template: `<todo-cmp (event)="fetch<caret>FromApi($event)"</todo-cmp>`,
})
export class TodoCmp {
    @Output()
    event = new EventEmitter<Bar>();
}

class Bar {
    foo() {}
}
