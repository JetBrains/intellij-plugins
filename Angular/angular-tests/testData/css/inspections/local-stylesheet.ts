// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core";

@Component({
    selector: 'todo-cmp',
    template: `
      <div class="local-class"></div>`,
    styles: [`
        .local-class {

        }

        <warning descr="Selector local-class2 is never used">.local-class2</warning> {

        }
    `]
})
export class TodoCmp {

}