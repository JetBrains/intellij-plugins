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
    protected readonly Colors = Colors;
    protected readonly Math = Math;
    protected readonly eval = eval;
}
