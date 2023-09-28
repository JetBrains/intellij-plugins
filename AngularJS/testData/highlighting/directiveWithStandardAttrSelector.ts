import {Component, Input, Directive, NgModule} from '@angular/core';

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
    <error descr="Property foo is not provided by any applicable directives nor by div element">[foo]</error>="value"
    ></div><blah test=<error descr="Type  \"12\"  is not assignable to type  number ">"12"</error>></blah>`,
  standalone: true,
})
export class TestComponentOne {
  value = "value";
  @Input()
  public test!: number
}
