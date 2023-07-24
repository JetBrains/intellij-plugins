import {Component, EventEmitter} from '@angular/core';

@Component({
  selector: 'app-test',
  outputs: ["output"],
  template: `
    <app-test (out<caret>put)="'foo'"></app-test>
  `
 })
export class TestComponent {

  output: EventEmitter<String>

}
