import {Component} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';

@Component({
   selector: 'nested-form-groups',
   template: `
        <form [formGroup]="form">
            <div formGroupName="newName">
                <input formControlName="first" placeholder="First name"/>
                <div formGroupName="foo">
                    <input formControlName="bar" placeholder="Last name"/>
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
     newName: new FormGroup({
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
    this.form.get(['newNa<caret>me', 'foo', 'bar'])
    this.form.get(['email', 'first'])
    this.form.get(['foo', 'first'])
    this.form.get('newName');
    this.form.get('newName.foo.bar');
    this.form.get('email.');
    this.form.get('foo.');
  }
}
