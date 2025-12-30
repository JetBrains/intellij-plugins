import {Component, NgModule, Input, Directive, Output, EventEmitter} from '@angular/core';

@Directive({standalone: true, selector: '[foo]'})
export class TestDir {
  @Input()
  protected protectedField!: string;
}

@Directive({standalone: true, selector: '[foo]'})
export class TestDir2 {
  @Input()
  protectedField!: string;
}

@Component({
   selector: 'cmp',
   template: `
      <div foo
           [protectedField]="value"
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
  protected protectedField!: string;
}
