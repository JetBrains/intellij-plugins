import {Component} from "@angular/core";
import {StandalonePipe} from "./standalone.pipe";

@Component({
    selector: "app-root",
    template: `
        <p>{{2 | standalone: 10}}</p>
    `,
    standalone: true,
    imports: [
        StandalonePipe
    ]
})
export class AppComponent {
}