import {Component, Inject} from '@angular/core';
import {FormArray, FormGroup, FormBuilder, Validators} from '@angular/forms';

@Component({
   selector: 'app-disabled-form-control',
   template: `
        <div [formGroup]="form">
            <div formControlName="last">
            <div formGroupName="name">
                <div formControlName="n<caret>ewName">
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
           newName: ['Nancy', Validators.minLength(2)],
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
    this.form.get("name.newName")
    this.form.get(["name", "newName"])
  }
}