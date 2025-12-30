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
       first: new FormControl('Foo'),
       last: new FormControl('Bar'),
     }),
     email: new FormControl(),
  });

  check() {
    this.form.get(['name', '<warning descr="Unrecognized Angular Form control, array or group name">foo</warning>', 'bar'])
    this.form.get(['email', '<warning descr="Unrecognized Angular Form control, array or group name">first</warning>'])
  }
}
