import {Directive, Component, Input} from '@angular/core';

@Directive({
 selector: '[app-dir]',
 standalone: true,
 inputs: ["inputt:alia<caret>sed"]
})
export class TestDirective {
  input: string
}

@Component({
 selector: 'app-test',
 hostDirectives: [
   {
     directive: TestDirective,
     inputs: ["aliased"]
   }
 ],
 template: `
    <app-test [aliased]="'foo'"></app-test>
    <div app-dir [aliased]="'foo'"></div>
  `
})
export class TestComponent {

}
