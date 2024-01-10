import {Component, Input} from '@angular/core';

@Component({
  selector: 'dest-component',
  template: `
    <div>
      <caret>
    </div>
  `
})
export class DestinationComponent {
  @Input()
  color: string

  @Input()
  value: Number
}
