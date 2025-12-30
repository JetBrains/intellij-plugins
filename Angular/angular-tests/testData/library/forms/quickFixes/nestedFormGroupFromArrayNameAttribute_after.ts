import {Component} from '@angular/core';
import {FormArray, FormControl, FormGroup, Validators} from '@angular/forms';

@Component({
   selector: 'nested-form-groups',
   template: `
        <form [formGroup]="form">
            <div formGroupName="name">
                <div formArrayName="first"/>
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
         first: new FormArray([])
     }),
     gender: new FormControl(),
  });
}
