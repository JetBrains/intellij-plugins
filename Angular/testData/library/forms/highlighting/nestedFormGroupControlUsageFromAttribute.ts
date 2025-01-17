import {Component} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';

@Component({
   selector: 'nested-form-groups',
   template: `
        <form [formGroup]="form">
            <div formGroupName="name">
                <input formControlName="first" placeholder="First name"/>
                <div formGroupName="foo">
                    <input formControlName="<usage>b<caret>ar</usage>" placeholder="Last name"/>
                </div>
                <div formGroupName="bar">
                    <input formControlName="baz" placeholder="Last name"/>
                </div>
            </div>
            <input formControlName="email" placeholder="Email"/>
        </form>
    `,
  standalone: false,
})
export class NestedFormGroupComp {
  form = new FormGroup({
     name: new FormGroup({
       first: new FormControl('Foo'),
       last: new FormControl('Bar'),
       foo: new FormGroup({
         <usage>bar</usage>: new FormControl('Baz')
       })
     }),
     gender: new FormControl(),
     address: new FormGroup({}),
     email: new FormControl(),
  });

  check() {
    this.form.get(['name', 'foo', '<usage>bar</usage>'])
    this.form.get(['email', 'first'])
    this.form.get('name');
    this.form.get('name.first');
    this.form.get('name.first');
    this.form.get('name.foo.<usage>bar</usage>');
  }
}
