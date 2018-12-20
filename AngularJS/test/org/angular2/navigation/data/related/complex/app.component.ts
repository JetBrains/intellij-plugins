// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';

import Styles from "./app.component.2.sass";
import template from "./app.component.html";

@Component({
    selector: 'app-root',
    template,
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

