import {Directive, Component, Output} from '@angular/core';

@Directive({
 selector: '[app-dir]',
 standalone: true,
})
export class TestDirective {
  @Output("newOutput")
  output: string
}

@Component({
 selector: 'app-test',
 hostDirectives: [
   {
     directive: TestDirective,
     outputs: ["new<caret>Output: aliasedTwice"]
   }
 ],
 template: `
    <app-test (aliasedTwice)="'foo'"></app-test>
    <div app-dir (newOutput)="'foo'"></div>
  `
})
export class TestComponent {

}
