@Component({selector: 'todo-cmp',
    template:`<a (click)="someMethod($e<caret>)"></a> `
})
export class TodoCmp {
    @Input() todo;
}
