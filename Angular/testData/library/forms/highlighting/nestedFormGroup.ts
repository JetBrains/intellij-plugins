import {Component} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';

@Component({
   selector: 'nested-form-groups',
   template: `
        <form [formGroup]="form">
            <div formGroupName="name">
                <input formControlName="first" placeholder="First name"/>
                <div formGroupName="foo">
                    <input formControlName="bar" placeholder="Last name"/>
                </div>
                <div formGroupName="<warning descr="Unrecognized name">bar</warning>">
                    <input formControlName="baz" placeholder="Last name"/>
                </div>
            </div>
            <input formControlName="email" placeholder="Email"/>
            <input formControlName="<warning descr="Unrecognized name">emails</warning>" placeholder="Email"/>
        </form>
        <form [formGroup]="foo">
            <input formControlName="email" placeholder="Email"/>
            <div formGroupName="name">
                <input formControlName="email" placeholder="Email"/>
            </div>
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
         bar: new FormControl('Baz')
       })
     }),
     gender: new FormControl(),
     address: new FormGroup({}),
     email: new FormControl(),
  });

  foo!: FormGroup

  check() {
    this.form.get(['name', 'foo', 'bar'])
    this.form.get(['name', 'foo', '<warning descr="Unrecognized name">baz</warning>', 'bar'])
    this.form.get(['email', '<warning descr="Unrecognized name">first</warning>', 'second'])
    this.form.get(['<warning descr="Unrecognized name">foo</warning>', 'first'])
    this.form.get('name');
    this.form.get('name.first');
    this.form.get('name.first.<warning descr="Missing name"></warning>');
    this.form.get('name.foo.bar');
    this.form.get('email.<warning descr="Unrecognized name">first</warning>.<warning descr="Unrecognized name">second</warning>');
    this.form.get('<warning descr="Unrecognized name">foo</warning>.<warning descr="Missing name"></warning>');

    this.foo.get('name');
  }
}
