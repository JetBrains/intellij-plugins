import {Component} from '@angular/core';

@Component({
  selector: 'app-test',
  inputs:['virtual'],
  template: `
    <app-test [vir<caret>tual]="'foo'"></app-test>
  `
 })
export class TestComponent {
}
