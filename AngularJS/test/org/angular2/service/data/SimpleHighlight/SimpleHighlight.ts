// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, NgModule} from '@angular/core';

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