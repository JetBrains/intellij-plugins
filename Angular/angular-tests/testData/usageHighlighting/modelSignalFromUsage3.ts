import {Component, model} from '@angular/core';

@Component({
   selector: 'app-root',
   standalone: true,
   template: `
   <app-root 
          [(<usage>theModel</usage>)]="sss"
          [<usage>theModel</usage>] = "ss"
          (<usage>the<caret>ModelChange</usage>)="dd"
   ></app-root>
   `,
 })
export class AppComponent {

  <usage>theModel</usage> = model.required()

}