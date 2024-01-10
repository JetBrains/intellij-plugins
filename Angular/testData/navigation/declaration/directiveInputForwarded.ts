import {Component} from '@angular/core';

@Component({
  selector: 'app-test',
  inputs: ["input"],
  template: `
    <app-test [in<caret>put]="'foo'"></app-test>
  `
 })
export class TestComponent {

  input: string

}
