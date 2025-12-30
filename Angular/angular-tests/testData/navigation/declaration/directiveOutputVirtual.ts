import {Component} from '@angular/core';

@Component({
  selector: 'app-test',
  outputs: ["virtual"],
  template: `
    <app-test (virt<caret>ual)="'foo'"></app-test>
  `
 })
export class TestComponent {
}
