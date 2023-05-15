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
  protected readonly eval = eval
}
