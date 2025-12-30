import {Component} from "@angular/core"

function customDecorator() {
    return function (ignored) {
    }
}

@customDecorator()
@Component({
    selector: 'foo-bar',
    templateUrl: './new-name.component.html',
    styleUrls: ['./new-name.component.css']
})
export class NewNameComponent {
}
