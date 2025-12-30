import {Directive, Component, Output} from '@angular/core';

@Directive({
 selector: '[app-dir]',
 standalone: true,
})
export class TestDirective {
  @Output("aliased")
  output: string
}

@Component({
 selector: 'app-test',
 hostDirectives: [
   {
     directive: TestDirective,
     outputs: ["aliased: newOutput"]
   }
 ],
 template: `
    <app-test (new<caret>Output)="'foo'"></app-test>
    <div app-dir (aliased)="'foo'"></div>
  `
})
export class TestComponent {

}
