import {Component} from "@angular/core"

@Component({
    selector: 'my-app',
    templateUrl: '/app/app.component.html',
    host: {
        '(click)': 'event',
        '[click]': 'binding',
        'click': 'attribute',
    }
})
export class AppComponent {
    onClick() {}
}
