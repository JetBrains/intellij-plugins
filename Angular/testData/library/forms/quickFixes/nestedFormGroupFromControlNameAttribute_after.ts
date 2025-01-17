import {Component} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';

@Component({
   selector: 'nested-form-groups',
   template: `
        <form [formGroup]="form">
            <div formGroupName="name">
                <input formControlName="first" placeholder="First name"/>
            </div>
            <input formControlName="email" placeholder="Email"/>
        </form>
    `,
  standalone: false,
})
export class NestedFormGroupComp {
  form = new FormGroup({
     name: new FormGroup({
       last: new FormControl('Bar'),
         first: new FormControl()
     }),
     gender: new FormControl(),
  });
}
