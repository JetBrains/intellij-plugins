import {
  Component,
  Directive,
  HostBinding,
  HostListener,
  Input,
} from '@angular/core';

@Component({
   selector: 'oy-chip',
   template: ``,
 })
export class ChipComponent {
  @HostBinding("style.align-<caret>")
  public small: boolean = false;
}