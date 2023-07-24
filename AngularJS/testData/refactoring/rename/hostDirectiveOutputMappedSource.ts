import {Directive, Component, Output} from '@angular/core';

@Directive({
 selector: '[app-dir]',
 standalone: true,
})
export class TestDirective {
  @Output("ali<caret>ased")
  output: string
}

@Component({
 selector: 'app-test',
 hostDirectives: [
   {
     directive: TestDirective,
     outputs: ["aliased: aliasedTwice"]
   }
 ],
 template: `
    <app-test (aliasedTwice)="'foo'"></app-test>
    <div app-dir (aliased)="'foo'"></div>
  `
})
export class TestComponent {

}
