import { Component, Input } from '@angular/core';

@Component({
  selector: 'm-button',
  template: '<button>{{someInput}}</button>'
})
export class ButtonComponent {
  @Input() someInput = '';
}
