import {Component} from '@angular/core';

@Component({
  selector: 'app-test',
  exportAs: "test",
  template: `
    <app-test ref-a='te<caret>st'></app-test>
  `
 })
export class TestComponent {
}
