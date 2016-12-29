import {Component} from '@angular/core';
import { NgModule } from '@angular/core';

@Component({
    selector: 'my-app' ,
    template: `{{title}}  text {{<error>title2</error>}} 
`
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