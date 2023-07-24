import {Directive, Component, Input} from '@angular/core';

@Directive({
 selector: '[app-dir]',
 standalone: true,
 inputs: ["input"]
})
export class TestDirective {
  inp<caret>ut: string
}

@Component({
 selector: 'app-test',
 hostDirectives: [
   {
     directive: TestDirective,
     inputs: ["input"]
   }
 ],
 template: `
    <app-test [input]="'foo'"></app-test>
    <div app-dir [input]="'foo'"></div>
  `
})
export class TestComponent {

}
