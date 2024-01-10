// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core";

@Component({
    selector: 'todo-cmp',
    templateUrl: './local-stylesheet-ext.html',
    styles: [`
        .local-class-html {

        }
        div.local-class-html {

        }
        <warning descr="Selector local-class-css is never used">.local-class-css</warning> {
            
        }
        <warning descr="Selector local-class-unused is never used">.local-class-unused</warning> {
            
        }
        #local-id-html {

        }
        <warning descr="Selector local-id-unused is never used">#local-id-unused</warning> {

        }
    `, `
        a<warning descr="Selector local-class-html is never used">.local-class-html</warning> {
            
        }
        <warning descr="Selector local-class-css is never used">.local-class-css</warning> {
            
        }
    `],
    styleUrls: [
        `local-stylesheet-ext.css`
    ]
})
export class TodoCmp {

}