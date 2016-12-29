import {Component} from '@angular/core';
import { NgModule } from '@angular/core';

@Component({
    selector: 'my-SimpleHighlightHtml',
    templateUrl: 'SimpleHighlightHtml.html',
})
export class AppComponent {
    title: number = 1;
}

let <error>z1111</error>:number = "";



@NgModule({
    declarations: [
        AppComponent,
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule { }