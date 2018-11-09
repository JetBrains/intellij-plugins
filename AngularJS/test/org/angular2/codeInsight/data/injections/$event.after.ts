@Component({selector: 'todo-cmp',
    template:`<a (click)="someMethod($event)"></a> `
})
export class TodoCmp {
    @Input() todo;
}
