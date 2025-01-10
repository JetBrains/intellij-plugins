import {
  Component,
  Input,
} from '@angular/core';

@Component({
   selector: 'oy-chip',
   template: ``,
   host: {
     "<caret>": "",
   }
 })
export class ChipComponent {
  @Input('small')
  public small: boolean = false;
}