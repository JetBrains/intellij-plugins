import {Component, Input} from '@angular/core';

@Component({
  selector: 'dest-component',
  template: `<div>
    {{eval('12') | currency}}
  </div>`
})
export class DestinationComponent {
  @Input()
  color: string

  @Input()
  value: Number
    protected readonly eval = eval;
}
