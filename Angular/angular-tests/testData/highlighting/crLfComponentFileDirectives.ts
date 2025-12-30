import {Component, Directive, input, InputSignal} from '@angular/core';

@Directive({
  selector: '[appExampleDirective]'
})
export class ExampleDirectiveDirective {
  public input =  input<{foo:number}>({foo:12})
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
    <app-base [input]="{foo:'112'}" />
    <app-extended [input]="'car'" />
  `,
  imports: [BaseComponent, ExtendedComponent],
})
export class AppComponent {


}
