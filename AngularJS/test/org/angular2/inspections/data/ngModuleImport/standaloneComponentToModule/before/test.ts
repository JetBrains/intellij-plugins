import {Component, NgModule} from "@angular/core";

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
  bootstrap: [AppComponent]
})
export class AppModule {
}