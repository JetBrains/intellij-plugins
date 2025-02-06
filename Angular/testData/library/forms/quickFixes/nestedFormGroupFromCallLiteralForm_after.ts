import {Component} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';

@Component({
   selector: 'nested-form-groups',
   template: ``,
  standalone: false,
})
export class NestedFormGroupComp {
  form = new FormGroup({
     name: new FormGroup({
       last: new FormControl('Bar'),
       foo: new FormGroup({
         bar: new FormControl('Baz')
       }),
         first: new FormGroup({
             
         })
     }),
     gender: new FormControl(),
     address: new FormGroup({}),
     email: new FormControl(),
  });

  check() {
    this.form.get('name');
    this.form.get('name.first');
  }
}
