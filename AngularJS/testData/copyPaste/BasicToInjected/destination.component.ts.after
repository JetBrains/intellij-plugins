import {Component, Input} from '@angular/core';
import {Colors} from "./colors";

@Component({
  selector: 'dest-component',
  template: `
    <div>
        <cdk-cell></cdk-cell>
        <div ngPlural></div>
        {{ Colors.red }}
        {{ Math.abs(12) }}
        <div [title]="eval('12') | currency"></div>
    </div>
  `
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
