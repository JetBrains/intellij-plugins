import {Directive, Component, Input} from '@angular/core';

@Directive({
  selector: 'app-dir',
  standalone: true,
})
export class TestDirective {
  @Input("aliased")
  input: string
}


@Component({
  selector: 'app-test',
  hostDirectives: [
    {
      directive: TestDirective,
      inputs: ["aliased: aliasedTwice"]
    }
  ],
  template: `
    <app-test [ali<caret>asedTwice]="'foo'"></app-test>
  `
})
export class TestComponent {

}
