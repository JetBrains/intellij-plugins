import {Component, Input, Directive } from '@angular/core';

@Directive({standalone: true, selector: '[foo]'})
export class TestDir {
  @Input()
  readonly readonlyField!: string;
}

@Directive({standalone: true, selector: '[foo]'})
export class TestDir2 {
  @Input()
  readonlyField!: string;
}

@Component({
   selector: 'cmp',
   template: `
      <div foo
           [readonlyField]="value"
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
  readonly readonlyField!: string;
}
