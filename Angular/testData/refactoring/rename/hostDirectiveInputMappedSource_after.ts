import {Directive, Component, Input} from '@angular/core';

@Directive({
 selector: '[app-dir]',
 standalone: true,
})
export class TestDirective {
  @Input("new<caret>Input")
  input: string
}

@Component({
 selector: 'app-test',
 hostDirectives: [
   {
     directive: TestDirective,
     inputs: ["newInput: aliasedTwice"]
   }
 ],
 template: `
    <app-test [aliasedTwice]="'foo'"></app-test>
    <div app-dir [newInput]="'foo'"></div>
  `
})
export class TestComponent {

}
