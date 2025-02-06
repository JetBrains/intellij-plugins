import {Component, Inject} from '@angular/core';
import {FormArray, FormGroup, FormBuilder, Validators} from '@angular/forms';

@Component({
   selector: 'app-disabled-form-control',
   template: `
        <div [formGroup]="form">
            <div formControlName="last">
            <div formGroupName="more">
                <div formArrayName="foo">
            </div>
        </div>`,
   standalone: false
})
export class DisabledFormControlComponent {

  form: FormGroup

  constructor(@Inject(FormBuilder) formBuilder: FormBuilder) {
    this.form = formBuilder.group(
      {
        name: formBuilder.group({
           first: ['Nancy', Validators.minLength(2)],
           last: 'foo',
           more: new FormGroup({
             foo: new FormArray([]),
             bar: formBuilder.control('bar'),
           })
         }),
        email: '',
      },
      {updateOn: 'change'},
    )
  }

  check() {
    this.form.get("name.more.foo")
    this.form.get(["name", "more", "foo"])
  }
}