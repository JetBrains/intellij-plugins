import {Component} from "@angular/core"

@Component({
    selector: 'todo-cmp',
    templateUrl: "./createFunction.html",
    template: `<div>{{fetchFromApi()}}</div>`,
})
export class TodoCmp {
    fetchFromApi() {
        
    }
}
