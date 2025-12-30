import { Component } from '@angular/core';
import {NgClass} from "@angular/common";

@Component({
  selector: 'app-ng-class-with-tailwind-util-classes',
  imports: [
    NgClass
  ],
  template: `
    <p
        [ngClass]="{
        'bar': true,
          'font-bold text-<caret>': true,
        }"
    >ng-class-with-tailwind-util-classes component</p>
  `,
  styleUrl: './ng-class-with-tailwind-util-classes.component.css',
  standalone: true
})
export class TailwindInNgClass {

}
