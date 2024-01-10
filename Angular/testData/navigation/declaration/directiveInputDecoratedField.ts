import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-test',
  template: `
    <app-test [in<caret>put]="'foo'"></app-test>
  `
 })
export class TestComponent {

  @Input()
  input: string

}
