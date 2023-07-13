import {Component, Directive, Input} from "@angular/core";

@Directive({
    standalone: true,
    selector: "[my-input]"
})
export class MyDirective {
    @Input("my-input")
    myInput: String
}

@Component({
    standalone: true,
    template: "<div my-<caret>input=''></div>",
    imports: [
        MyDirective
    ]
})
class MyComponent {
}
