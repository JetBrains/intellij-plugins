import {Component} from '@angular/core';

@Component({
   selector: 'app-foo',
   template: `
    @for(item of items; track item.name; let index = $index<caret>)
   `,
   standalone: true
 })
export class AppComponent {
  items!: { name: string }[];
}
