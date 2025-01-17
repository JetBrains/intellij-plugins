import {Component} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';

@Component({
   selector: 'nested-form-groups',
   template: ``,
  standalone: false,
})
export class NestedFormGroupComp {
  form = new FormGroup({
     name: new FormGroup({
       first: new FormControl('Foo'),
       last: new FormControl('Bar'),
       foo: new FormGroup({
         bar: new FormControl('Baz')
       })
     }),
     gender: new FormControl(),
     address: new FormGroup({}),
     email: new FormControl(),
  });

  check() {
    this.form.get(['name', 'foo', 'bar'])
    this.form.get(['email', 'first'])
    this.form.get(['foo', 'first'])
  }
}
