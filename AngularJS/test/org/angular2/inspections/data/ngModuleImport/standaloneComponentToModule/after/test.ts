import {Component, NgModule} from "@angular/core";
import {StandaloneComponent} from "./standalone.component";

@Component({
    selector: "app-root",
    template: `
        <app-standalone></app-standalone>
    `,
})
export class AppComponent {
}

@NgModule({
    declarations: [
        AppComponent,
    ],
    imports: [
        StandaloneComponent
    ],
    bootstrap: [AppComponent]
})
export class AppModule {
}