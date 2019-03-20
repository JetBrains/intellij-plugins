class Bar {
    foo() {}
}

@Component({
    templateUrl: "./createFieldWithExplicitPublic.html",
    selector: 'todo-cmp'
})
export class TodoCmp {
    public unresolved: any;
}
