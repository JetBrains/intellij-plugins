import {Component, EventEmitter} from '@angular/core';

@Component({
  selector: 'app-test',
  outputs: ["output : aliased"],
  template: `
    <app-test (ali<caret>ased)="'foo'"></app-test>
  `
 })
export class TestComponent {

  output: EventEmitter<String>

}
