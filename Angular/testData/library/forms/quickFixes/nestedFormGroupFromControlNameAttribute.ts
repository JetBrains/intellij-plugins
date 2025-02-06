import {Component} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';

@Component({
   selector: 'nested-form-groups',
   template: `
        <form [formGroup]="form">
            <div formGroupName="name">
                <input formControlName="<warning descr="Unrecognized name">first</warning>" placeholder="First name"/>
            </div>
            <input formControlName="<warning descr="Unrecognized name">email</warning>" placeholder="Email"/>
        </form>
    `,
  standalone: false,
})
export class NestedFormGroupComp {
  form = new FormGroup({
     name: new FormGroup({
       last: new FormControl('Bar'),
     }),
     gender: new FormControl(),
  });
}
