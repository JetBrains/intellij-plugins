import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-component',
  template: `<div>
    <cdk-cell [title]="eval('12') | currency"></cdk-cell>
</div>
`
})
export class SourceComponent {
  @Input()
  color: string

  @Input()
  value: Number
  protected readonly eval = eval
}
