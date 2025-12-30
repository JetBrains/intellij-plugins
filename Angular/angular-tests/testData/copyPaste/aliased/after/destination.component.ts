import {Component, Input} from '@angular/core';
import {Colors} from "./colors";

@Component({
  selector: 'dest-component',
  templateUrl: './destination.component.html'
})
export class DestinationComponent {
  @Input()
  color: string

  @Input()
  value: Number
    protected readonly Colors1 = Colors;
    protected readonly Math1 = Math;
    protected readonly eval1 = eval;
}
