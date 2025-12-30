import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  Output,
} from '@angular/core';

@Component({
   selector: 'oy-chip',
 })
export class ChipComponent {

  @Output()
  public clang = new EventEmitter<string>()

  @HostListener('cl<caret>', ['$event'])
  onKeyDown($event: string) {
  }
}