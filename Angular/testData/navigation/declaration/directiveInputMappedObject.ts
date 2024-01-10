import {Component} from '@angular/core';

@Component({
  selector: 'app-test',
  inputs:[{name: "input", alias: "aliased"}],
  template: `
    <app-test [ali<caret>ased]="'foo'"></app-test>
  `
 })
export class TestComponent {

  input: string

}
