import {Component} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';

@Component({
   selector: 'nested-form-groups',
   templateUrl:"./nestedFormGroupFromControlNameAttributeExternal.html",
   standalone: false,
})
export class NestedFormGroupComp {
  form = new FormGroup({
     name: new FormGroup({
       last: new FormControl('Bar'),
         first: new FormControl()
     }),
     gender: new FormControl(),
  });
}
