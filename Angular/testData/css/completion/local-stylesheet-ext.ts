// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core";

@Component({
    selector: 'todo-cmp',
    templateUrl: './local-stylesheet-ext.html',
    styles: [`
        div.local-class-int {

        }

        #local-id-int {

        }
    `, `
        . {

        }
    `, `
        # {

        }
    `],
    styleUrls: [
        `./local-stylesheet-ext.css`,
        `./local-stylesheet-ext-absent.css`
    ]
})
export class TodoCmp {

}
