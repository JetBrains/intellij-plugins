import {
  Component,
} from '@angular/core';

@Component({
   selector: 'oy-chip',
   template: ``,
   styles: `
    .oy-chip {
      &.oy-chip--small {
      }
    }`,
   host: {
     "class": "<caret>",
   }
 })
export class ChipComponent {
}