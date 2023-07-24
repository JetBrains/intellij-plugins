import {Directive, Component, Input} from '@angular/core';

@Directive({
 selector: '[app-dir]',
 standalone: true,
})
export class TestDirective {
  @Input("aliased")
  input: string
}

@Component({
 selector: 'app-test',
 hostDirectives: [
   {
     directive: TestDirective,
     inputs: ["aliasd: newInpu<caret>t"]
   }
 ],
 template: `
    <app-test [newInput]="'foo'"></app-test>
    <div app-dir [aliased]="'foo'"></div>
  `
})
export class TestComponent {

}
