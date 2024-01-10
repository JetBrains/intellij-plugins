import {Directive, Component, Output, EventEmitter} from '@angular/core';

@Directive({
  selector: 'app-dir',
  standalone: true,
})
export class TestDirective {
  @Output("aliased")
  output: EventEmitter<String>
}

@Component({
  selector: 'app-test',
  hostDirectives: [
    {
      directive: TestDirective,
      outputs: ["aliased"]
    }
  ],
  template: `
    <app-test (ali<caret>ased)="'foo'"></app-test>
  `
 })
export class TestComponent {

}
