import {Component, inject} from '@angular/core';
import {FormGroup, FormBuilder, Validators} from '@angular/forms';

@Component({
   selector: 'app-disabled-form-control',
   template: `
        <div [formGroup]="form">
            <div formGroupName="name">
                <div formGroupName="more">
                    <div formControlName="<warning descr="Unrecognized name">foo</warning>">
                    </div>
                </div>
            </div>
        </div>`,
   standalone: false
})
export class DisabledFormControlComponent {
  private formBuilder = inject(FormBuilder);

  form = this.formBuilder.group(
    {
      name: this.formBuilder.group({
         first: ['Nancy', Validators.minLength(2)],
         last: 'foo',
         more: new FormGroup({
           bar: this.formBuilder.control('bar'),
         })
      }),
      email: '',
    },
    {updateOn: 'change'},
  )
}