import {Component} from "@angular/core";

@Component({
    selector: "app-root",
    template: `
        <p>{{2 | standalone: 10}}</p>
    `,
    standalone: true,
})
export class AppComponent {
}