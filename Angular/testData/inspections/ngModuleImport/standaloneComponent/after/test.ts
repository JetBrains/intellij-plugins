import {Component} from "@angular/core";
import {StandaloneComponent} from "./standalone.component";

@Component({
    selector: "app-root",
    template: `
        <app-standalone></app-standalone>
    `,
    standalone: true,
    imports: [
        StandaloneComponent
    ]
})
export class AppComponent {
}