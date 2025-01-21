import {Component} from '@angular/core';
import {FormControl, FormGroup, FormArray} from '@angular/forms';

@Component({
   selector: 'nested-form-groups',
   template: `
        <form [formGroup]="form">
            <form formGroupName="group">
                <input formControlName="members" placeholder="First name"/>
                <div formArrayName="<usage>mem<caret>bers</usage>">
                    <input formControlName="23" placeholder="Email"/>
                </div>
            </form>
        </form>
    `,
  standalone: false,
})
export class NestedFormGroupComp {
  form = new FormGroup({
     first: new FormArray([new FormControl('Foo'), new FormControl('Foo')]),
     group: new FormGroup({
       name: new FormControl(),
       <usage>members</usage>: new FormArray([new FormControl('Foo'), new FormControl('Foo')])
     }),
  });

  check() {
    this.form.get(['first', 'members'])
    this.form.get(['group', '<usage>members</usage>', '12'])
    this.form.get('first.members')
    this.form.get('group.<usage>members</usage>.234')
  }
}
