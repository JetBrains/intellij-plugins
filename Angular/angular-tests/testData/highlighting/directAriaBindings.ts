import {Component, Directive} from "@angular/core"

@Directive({selector: '[myDir]'})
class MyDir {}

@Component({
  selector: 'app-input-test',
  imports: [MyDir],
  template: `
    <input myDir 
          [attr.aria-disabled]="disabled" 
          [aria-readonly]="readonly" 
          [ariaLabel]="label">
    <input myDir 
          [attr.<warning descr="Unrecognized HTML Attribute">aria-disaled</warning>]="disabled" 
          <error descr="Property aria-readnly is not provided by any applicable directives nor by <input> element">[aria-readnly]</error>="readonly" 
          <error descr="Property ariaLbel is not provided by any applicable directives nor by <input> element">[ariaLbel]</error>="label">
    
    <button aria-label="{{label}} menu"></button>
    <button <warning descr="Attribute aria-labl is not allowed here">aria-labl</warning>="{{label}} menu"></button>
  `,
})
export class ParentComponent {
  disabled = '';
  readonly = '';
  label = '';
}