import {Directive, Component, Input} from '@angular/core';

@Directive({
 selector: '[app-dir]',
 standalone: true,
 inputs: ["inputt:newI<caret>nput"]
})
export class TestDirective {
  input: string
}

@Component({
 selector: 'app-test',
 hostDirectives: [
   {
     directive: TestDirective,
     inputs: ["newInput"]
   }
 ],
 template: `
    <app-test [newInput]="'foo'"></app-test>
    <div app-dir [newInput]="'foo'"></div>
  `
})
export class TestComponent {

}
