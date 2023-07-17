import {Component, Directive, Input} from '@angular/core';

// @ts-ignore
@Directive({
   selector: '[appBold]',
   standalone: true,
   exportAs: "bold"
 })
export class BoldDirective {
  @Input()
  weight: Number = 12
}

@Component({
   standalone: true,
   selector: 'app-test',
   template: `
      <app-test font<caret>Weight='13'></app-test>
   `,
   hostDirectives: [{
     directive: BoldDirective,
     inputs: ['weight: fontWeight']
   }],
 })
export class TestComponent  {
}
