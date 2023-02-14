import {Component, Input} from '@angular/core';
import {Colors} from "./colors";

@Component({
  selector: 'app-component',
  template: `
    <div>
      <selection><cdk-cell></cdk-cell>
        <div ngPlural></div>
        {{ Colors.red }}
        {{ Math.abs(12) }}
        <div [title]="eval('12') | currency"></div></selection>
    </div>
  `
})
export class Injected {
  @Input()
  color: string

  @Input()
  value: Number
  protected readonly Colors = Colors;
  protected readonly Math = Math;
  protected readonly eval = eval
}
