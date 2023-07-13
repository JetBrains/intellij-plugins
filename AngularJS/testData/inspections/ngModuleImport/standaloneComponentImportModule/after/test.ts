import {Component} from "@angular/core";
import {ClassicModule} from "./classic";

@Component({
    selector: "app-root",
    template: `
        <app-classic></app-classic>
    `,
    standalone: true,
    imports: [
        ClassicModule
    ]
})
export class AppComponent {
}