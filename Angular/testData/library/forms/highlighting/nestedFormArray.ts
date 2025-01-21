import {Component} from '@angular/core';
import {FormControl, FormGroup, FormArray} from '@angular/forms';

@Component({
   selector: 'nested-form-groups',
   template: `
        <form [formGroup]="form">
            <div formGroupName="<warning descr="Unrecognized name">first</warning>">
                <input formControlName="first" placeholder="First name"/>
            </div>
            <div formArrayName="first">
                <input formControlName="<warning descr="Unrecognized name">first</warning>" placeholder="First name"/>
                <input formControlName="12" placeholder="First name"/>
                <div formGroupName="<warning descr="Unrecognized name">12</warning>"></div>
                <div formArrayName="<warning descr="Unrecognized name">12</warning>"></div>
            </div>
            <form formGroupName="group">
                <input formControlName="<warning descr="Unrecognized name">email</warning>" placeholder="Email"/>
                <div formArrayName="members">
                    <input formControlName="<warning descr="Unrecognized name">email</warning>" placeholder="Email"/>
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
       members: new FormArray([new FormControl('Foo'), new FormControl('Foo')])
     }),
  });

  check() {
    this.form.get(['first', '1'])
    this.form.get(['first', '<warning descr="Unrecognized name">foo</warning>'])
    this.form.get(['group', 'members', '<warning descr="Unrecognized name">23.4</warning>'])
    this.form.get(['first', '1'])
    this.form.get(['first', '<warning descr="Unrecognized name">foo</warning>', 'bar'])
    this.form.get(['group', 'members', '<warning descr="Unrecognized name">23.4</warning>'])
    this.form.get('first.1')
    this.form.get('first.<warning descr="Unrecognized name">foo</warning>')
    this.form.get('group.members.23.4')
    this.form.get('first.1')
    this.form.get('first.<warning descr="Unrecognized name">foo</warning>.<warning descr="Unrecognized name">bar</warning>')
    this.form.get('group.members.23.4')
  }
}
