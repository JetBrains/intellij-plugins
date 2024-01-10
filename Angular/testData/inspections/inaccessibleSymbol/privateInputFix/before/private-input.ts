import {Component, NgModule, Input, Directive, Output, EventEmitter} from '@angular/core';

@Directive({standalone: true, selector: '[foo]'})
export class TestDir {
  @Input()
  private privateField!: string;
}

@Directive({standalone: true, selector: '[foo]'})
export class TestDir2 {
  @Input()
  privateField!: string;
}

@Component({
   selector: 'cmp',
   template: `
      <div foo
           [privateField]="value"
      ></div>
    `,
   imports: [
     TestDir, TestDir2
   ],
   standalone: true
 })
export class TestComponent {
  value = "value";

  @Input()
  private privateField!: string;

}
