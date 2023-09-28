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
    template: '<div bar [theInput]="value"></div>',
    standalone: true,
    imports: [
        TestDir
    ]
})
export class TestComponentOne {
  value = "value";
}
