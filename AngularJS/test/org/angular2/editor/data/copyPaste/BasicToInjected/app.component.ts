import {Component, Input} from '@angular/core';
import {Colors} from "./colors";

@Component({
  selector: 'app-component',
  templateUrl: './basicToInjected.html'
})
export class AppComponent {
  @Input()
  color: string

  @Input()
  value: Number
  protected readonly Colors = Colors;
  protected readonly Math = Math;
  protected readonly eval = eval
}
