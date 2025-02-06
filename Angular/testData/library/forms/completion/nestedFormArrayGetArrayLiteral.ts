import {Component} from '@angular/core';
import {FormControl, FormGroup, FormArray} from '@angular/forms';

@Component({
   selector: 'nested-form-groups',
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

  check() {
    this.form.get(['first', '12'])
    this.form.get(['group', 'members', '12'])
  }
}
