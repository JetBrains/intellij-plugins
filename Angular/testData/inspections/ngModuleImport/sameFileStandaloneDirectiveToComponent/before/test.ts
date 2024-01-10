import {Component, Input, Directive} from '@angular/core';

@Directive({
   standalone: true,
   selector: '[bar]'
 })
export class TestDir {
  @Input()
  readonly theInput!: string;
}

@Component({
   selector: 'blah',
   template: '<div bar [the<caret>Input]="value"></div>',
   standalone: true,
 })
export class TestComponentOne {
  value = "value";
}
