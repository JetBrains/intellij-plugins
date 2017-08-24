import {Component, NgModule} from '@angular/core';

@Component({
    selector: 'my-SimpleHighlightHtml',
    templateUrl: 'SimpleHighlightHtml.html',
})
export class AppComponent {
    title: number = 1;
}

let z1111:number = "";



@NgModule({
    declarations: [
        AppComponent,
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule { }