import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'lib-button',
  standalone: false,
  template: '<button [attr.disabled]="disabled || undefined" (click)="clicked.emit($event)"><ng-content/></button>',
})
export class ButtonComponent {

  @Input() disabled: boolean = false;
  @Output() clicked = new EventEmitter<MouseEvent>();
}
