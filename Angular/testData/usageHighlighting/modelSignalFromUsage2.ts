import {Component, model} from '@angular/core';

@Component({
   selector: 'app-root',
   standalone: true,
   template: `
   <app-root 
          [(<usage>theModel</usage>)]="sss"
          [<usage>the<caret>Model</usage>] = "ss"
          (<usage>theModelChange</usage>)="dd"
   ></app-root>
   `,
 })
export class AppComponent {

  <usage>theModel</usage> = model.required()

}