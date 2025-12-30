import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-test',
  template: `
    <app-test [ali<caret>ased]="'foo'"></app-test>
  `
 })
export class TestComponent {

  @Input("aliased")
  input: string

}
