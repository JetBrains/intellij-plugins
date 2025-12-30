import {Component} from '@angular/core';

@Component({
   selector: 'app-foo',
   template: `
    @for(<caret>)
   `,
   standalone: true
 })
export class AppComponent {
  items!: { name: string }[];
}
