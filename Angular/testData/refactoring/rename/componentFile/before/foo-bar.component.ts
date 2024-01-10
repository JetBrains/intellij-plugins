import {Component} from "@angular/core"

function customDecorator() {
    return function (ignored) {
    }
}

@customDecorator()
@Component({
    selector: 'foo-bar',
    templateUrl: './foo-bar.component.html',
    styleUrls: ['./foo-bar.component.css']
})
export class FooBarComponent {
}
