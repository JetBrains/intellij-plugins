import {Component, Directive} from "@angular/core"

@Directive({selector: '[myDir]'})
class MyDir {}

@Component({
  selector: 'app-input-test',
  imports: [MyDir],
  template: `
    <input myDir 
          <error descr="Aria property aliases are supported only in Angular 20.2 and above.">[aria-readonly]</error>="readonly" 
          [ariaLabel]="label">
  `,
})
export class ParentComponent {
  readonly = '';
  label = '';
}