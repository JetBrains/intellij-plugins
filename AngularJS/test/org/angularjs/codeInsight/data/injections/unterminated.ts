@Component({selector: 'todo-cmp',
    template:`{{todo}}`
})
export class TodoCmp {
    @Input() todo;
}
