import {Component} from "@angular/core";

@Component({
    selector: "app-root",
    template: `
        <app-standalone></app-standalone>
    `,
    standalone: true,
})
export class AppComponent {
}