// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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