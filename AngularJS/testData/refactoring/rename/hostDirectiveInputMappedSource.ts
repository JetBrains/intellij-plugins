import {Directive, Component, Input} from '@angular/core';

@Directive({
 selector: '[app-dir]',
 standalone: true,
})
export class TestDirective {
  @Input("ali<caret>ased")
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
    <app-test [aliasedTwice]="'foo'"></app-test>
    <div app-dir [aliased]="'foo'"></div>
  `
})
export class TestComponent {

}
