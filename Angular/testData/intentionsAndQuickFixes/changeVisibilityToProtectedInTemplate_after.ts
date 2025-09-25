import {Component} from '@angular/core';

@Component({
  selector: 'app-test',
  template: `
    {{ foo }}
  `
})
export class TestComponent {
  protected f<caret>oo!: string;
}