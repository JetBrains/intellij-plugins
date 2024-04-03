import {Component, model} from '@angular/core';

@Component({
   selector: 'app-root',
   standalone: true,
   template: `
   <app-root 
          [(<usage>aliasedModel</usage>)]="sss"
          [<usage>aliasedModel</usage>] = "ss"
          (<usage>aliased<caret>ModelChange</usage>)="dd"
   ></app-root>
   `,
 })
export class AppComponent {

  modelWithAlias = model.required( {alias: "<usage>aliasedModel</usage>"})

}