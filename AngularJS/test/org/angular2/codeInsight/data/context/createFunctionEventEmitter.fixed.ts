import {Component} from "@angular/core"

@Component({
    selector: 'todo-cmp',
    template: `<todo-cmp (event)="fetchFromApi($event)"</todo-cmp>`,
})
export class TodoCmp {
    @Output()
    event: EventEmitter<Bar>;

    fetchFromApi($event: Bar) {

    }
}

class Bar {
    foo() {}
}
