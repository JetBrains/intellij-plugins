import {Component, Input, Directive} from '@angular/core';

@Directive({standalone: true, selector: '[foo]'})
export class TestDir {
  @Input()
  publicField!: string;
  @Input()
  protected protectedField!: string;
  @Input()
  private privateField!: string;
  @Input()
  readonly readonlyField!: string;
}

@Component({
             selector: 'blah',
             template: `<div foo
                    [publicField]="value"
                    [readonlyField]="value" 
                    [protectedField]="value"
                    [privateField]="value"
               ></div>`,
             standalone: true,
             imports: [
               TestDir
             ],
           })
export class FooCmp {
  value = "value";
}