import {Component} from "@angular/core"

class EventEmitter<T> {}

@Component({
    selector: 'todo-cmp',
    template: `<todo-cmp (event)="fetchFromApi($event)"</todo-cmp>`,
})
export class TodoCmp {
    @Output()
    event = new EventEmitter<Bar>();

    protected fetchFromApi($event: Bar) {

    }
}

class Bar {
    foo() {}
}
