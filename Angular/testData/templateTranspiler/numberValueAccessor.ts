import {Component} from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';

@Component({
 standalone: true,
 selector: 'datepicker-views-selection-example',
 template: `
    <input type="number" [formControl]="totalCountControl">
  `,
 imports: [
   ReactiveFormsModule,
 ]
})
export class DatepickerViewsSelectionExample {
  totalCountControl = new FormControl();
}

