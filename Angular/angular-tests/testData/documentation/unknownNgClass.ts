import { Component } from '@angular/core';
import {NgClass} from "@angular/common";

@Component({
  selector: 'app-ng-class',
  imports: [
    NgClass
  ],
  template: `
    <p
        [ngClass]="{
        'bar': true,
          'font-bold text-<caret>align': true,
        }"
    ></p>
  `,
  standalone: true
})
export class UnknownNgClass {

}
