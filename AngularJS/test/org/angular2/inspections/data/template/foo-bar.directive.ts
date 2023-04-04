import {Directive, Input} from '@angular/core';

@Directive({
  selector: '[appFooBar]'
})
export class FooBarDirective {

  constructor() { }

  @Input({required: true})
  appFooBar: string = ""

  @Input({required: true})
  appFooBar2: string = ""

}
