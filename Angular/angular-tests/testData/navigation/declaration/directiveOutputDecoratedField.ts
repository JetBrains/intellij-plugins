import {Component, Output, EventEmitter} from '@angular/core';

@Component({
  selector: 'app-test',
  template: `
    <app-test (out<caret>put)="'foo'"></app-test>
  `
 })
export class TestComponent {

  @Output()
  output: EventEmitter<String>

}
