import {
  Component,
  HostBinding,
  HostListener,
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
     "[class.<caret>": "small",
   }
 })
export class ChipComponent {
  @Input('small')
  public small: boolean = false;
}