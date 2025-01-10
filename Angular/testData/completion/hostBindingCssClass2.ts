import {
  Component,
  Input,
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
     "class": "oy-chip oy<caret>",
   }
 })
export class ChipComponent {
}