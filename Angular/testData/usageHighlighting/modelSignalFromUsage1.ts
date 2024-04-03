import {Component, model} from '@angular/core';

@Component({
   selector: 'app-root',
   standalone: true,
   template: `
   <app-root 
          [(<usage>the<caret>Model</usage>)]="sss"
          [<usage>theModel</usage>] = "ss"
          (<usage>theModelChange</usage>)="dd"
   ></app-root>
   `,
 })
export class AppComponent {

  modelWithAlias = model.required( {alias: "<usage>theModel</usage>"})

}