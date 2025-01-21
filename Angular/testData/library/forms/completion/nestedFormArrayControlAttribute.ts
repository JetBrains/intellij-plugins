import {Component} from '@angular/core';
import {FormControl, FormGroup, FormArray} from '@angular/forms';

@Component({
   selector: 'nested-form-groups',
   template: `
        <form [formGroup]="form">
            <div formArrayName="first">
                <input formControlName="12" placeholder="First name"/>
            </div>
            <div formGroupName="group">
              <div formArrayName="members">
                  <input formControlName="23" placeholder="First name"/>
              </div>
            </div>
        </form>
    `,
  standalone: false,
})
export class NestedFormGroupComp {
  form = new FormGroup({
     first: new FormArray([new FormControl('Foo'), new FormControl('Foo')]),
     group: new FormGroup({
       name: new FormControl(),
       members: new FormArray([new FormControl('Foo'), new FormControl('Foo')])
     }),
  });
}
