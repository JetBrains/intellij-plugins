import {Directive, Component, Input} from '@angular/core';

@Directive({
 selector: '[app-dir]',
 standalone: true,
 inputs: ["newInput"]
})
export class TestDirective {
  newInp<caret>ut: string
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
