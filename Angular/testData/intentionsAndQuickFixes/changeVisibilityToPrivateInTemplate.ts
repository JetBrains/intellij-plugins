import {Component} from '@angular/core';

@Component({
  selector: 'app-test',
  template: `
    {{ foo }}
  `
})
export class TestComponent {
  f<caret>oo!: string;
}