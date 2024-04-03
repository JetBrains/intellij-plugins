import {Component, model} from '@angular/core';

@Component({
   selector: 'app-root',
   standalone: true,
   template: `
   <app-root 
          [(<usage>aliased<caret>Model</usage>)]="sss"
          [<usage>aliasedModel</usage>] = "ss"
          (<usage>aliasedModelChange</usage>)="dd"
   ></app-root>
   `,
 })
export class AppComponent {

  modelWithAlias = model.required( {alias: "<usage>aliasedModel</usage>"})

}