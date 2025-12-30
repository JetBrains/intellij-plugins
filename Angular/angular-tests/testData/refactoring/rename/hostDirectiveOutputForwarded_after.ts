import {Directive, Component, Output} from '@angular/core';

@Directive({
 selector: '[app-dir]',
 standalone: true,
})
export class TestDirective {
  @Output("new<caret>Output")
  output: string
}

@Component({
 selector: 'app-test',
 hostDirectives: [
   {
     directive: TestDirective,
     outputs: ["newOutput"]
   }
 ],
 template: `
    <app-test (newOutput)="'foo'"></app-test>
    <div app-dir (newOutput)="'foo'"></div>
  `
})
export class TestComponent {

}
