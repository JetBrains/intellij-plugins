import {Component, Input} from '@angular/core';

@Component({selector: 'todo-cmp',
    template:`{{todo}}`
})
export class TodoCmp {
    @Input() todo!: string;
}
