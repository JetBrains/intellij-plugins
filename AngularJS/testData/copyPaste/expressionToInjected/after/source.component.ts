import {Component, Input} from '@angular/core';

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
