// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';

import Styles from "./app.component.2.sass";

@Component({
    selector: 'app-root',
    template: require("./app.component.html"),
    styleUrls: ['./app.component.css'],
    styles: [`
        inline1 {

        }`,
        Styles,
            `
            inline2 {

            }`]
})
export class AppComponent {
    title = 'untitled35';
}

