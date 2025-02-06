import {Component} from '@angular/core';
import {FormControl, FormGroup, <weak_warning descr="TS6133: 'Validators' is declared but its value is never read.">Validators</weak_warning>} from '@angular/forms';

@Component({
   selector: 'nested-form-groups',
   template: `
        <form [formGroup]="form">
            <div formGroupName="name">
                <div formGroupName="<warning descr="Unrecognized name">first</warning>"/>
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
