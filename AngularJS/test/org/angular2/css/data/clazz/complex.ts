// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core";

@Component({
    selector: 'todo-cmp',
    templateUrl: `./complex.html`,
    template: `
      <style>
        .inline-html-class {

        }
      </style>
      <div class=""></div>`,
    styles: [`
        .inline-class {

        }`],
    styleUrls: ["./complex-internal.css"]
})
export class TodoCmp {

}