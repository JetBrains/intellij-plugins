import {Directive, Component, Input} from '@angular/core';

@Directive({
 selector: '[app-dir]',
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
     inputs: ["aliased: aliased<caret>Twice"]
   }
 ],
 template: `
    <app-test aliasedTwice="'foo'"></app-test>
    <app-test [aliasedTwice]="'foo'"></app-test>
  `
})
export class TestComponent {

}
