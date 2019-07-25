// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

@Component({
    selector: 'app-simple-form',
    template: `
        <form #f = "ngForm"
              (ngSubmit) = "onSubmit(f)"
              novalidate>
            <input name = "first"
                   ngModel
                   required
                   #first = "ngModel">
            <input name = "last"
                   ngModel>
            <button>Submit</button>
        </form>

        <p>First name value: {{ first.value }}</p>
        <p>First name valid: {{ first.valid }}</p>
        <p>Form value: {{ f.value | json }}</p>
        <p>Form valid: {{ f.valid }}</p>
    `,
})
export class SimpleFormComponent {
    onSubmit(f: NgForm) {
        console.log(f.value);  // { first: '', last: '' }
        console.log(f.valid);  // false
    }
}
