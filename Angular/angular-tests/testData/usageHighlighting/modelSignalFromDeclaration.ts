import {Component, model} from '@angular/core';

@Component({
   selector: 'app-root',
   standalone: true,
   template: `
   <app-root 
          [(<usage>theModel</usage>)]="sss"
          [<usage>theModel</usage>] = "ss"
          (<usage>theModelChange</usage>)="dd"
   ></app-root>
   `,
 })
export class AppComponent {

  <usage>the<caret>Model</usage> = model.required()

}