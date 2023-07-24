import {Directive, Output, EventEmitter} from '@angular/core';

@Directive({
   selector: '[appBold]',
   standalone: true,
   outputs: ["field1: alias1", "virtual"],
 })
export class BoldDirective {
  field1 = new EventEmitter<String>()
  field2  = new EventEmitter<String>()

  @Output("alias3")
  field3 = new EventEmitter<Number>()

  @Output()
  field4 = new EventEmitter<String>()
}

@Directive({
   selector: '[appMouseenter]',
   standalone: true,
   hostDirectives: [{
     directive: BoldDirective,
     outputs: ['<caret>']
   }]
 })
export class MouseenterDirective {
}
