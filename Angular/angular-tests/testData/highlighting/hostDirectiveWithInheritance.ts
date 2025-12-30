import {Component, Directive, input} from '@angular/core';

@Directive({
  selector: '[appExampleDirective]'
})
export class ExampleDirectiveDirective {
  public input = input(true)
}

@Component({
  selector: 'app-base',
  hostDirectives: [{directive: ExampleDirectiveDirective, inputs: ['input']}],
  template: '',
})
export class BaseComponent {

}

@Component({
  selector: 'app-extended',
  template: '',
})
export class ExtendedComponent extends BaseComponent {

}

@Component({
  selector: 'app-root',
  template: `
    <app-base [input]="true" />
    <app-extended [input]="true" />
    <app-extended <error descr="Property foo is not provided by any applicable directives nor by <app-extended> element">[foo]</error>="true" />
  `,
  imports: [BaseComponent, ExtendedComponent],
})
export class AppComponent {
}
