import {Component, Input} from '@angular/core';
import {Colors} from "./colors";

@Component({
  selector: 'app-component',
  templateUrl: './source.component.html'
})
export class SourceComponent {
  @Input()
  color: string

  @Input()
  value: Number
  protected readonly Colors = Colors;
  protected readonly Math = Math;
  protected readonly eval = eval
  protected readonly und = undefined

  protected readonly Colors2 = Colors;
  protected readonly Math2 = Math;
  protected readonly eval2 = eval
}
