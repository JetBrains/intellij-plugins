import {Component, Output, EventEmitter} from '@angular/core';

@Component({
  selector: 'app-test',
  template: `
    <app-test (ali<caret>ased)="'foo'"></app-test>
  `
 })
export class TestComponent {

  @Output("aliased")
  output: EventEmitter<String>

}
