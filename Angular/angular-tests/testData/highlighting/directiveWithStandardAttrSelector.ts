import {Component, Input, Directive} from '@angular/core';

@Directive({
             standalone: true,
             selector: '[title]'
           })
export class TestDir {
  @Input()
  public title!: number
}

@Component({
  selector: 'blah',
  template: `<div 
    title="Foo" 
    <error descr="Property foo is not provided by any applicable directives nor by <div> element">[foo]</error>="value"
    ></div><blah <error descr="TS2322: Type 'string' is not assignable to type 'number'.">test</error>="12"></blah>`,
  standalone: true,
})
export class TestComponentOne {
  value = "value";
  @Input()
  public test!: number
}
