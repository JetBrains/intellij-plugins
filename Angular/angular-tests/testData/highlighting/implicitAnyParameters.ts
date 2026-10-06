import {Component} from '@angular/core';

function sum(<error descr="TS7006: Parameter 'a' implicitly has an 'any' type.">a</error>, <error descr="TS7006: Parameter 'b' implicitly has an 'any' type.">b</error>) {
  return a + b;
}

const multiply = (<error descr="TS7006: Parameter 'a' implicitly has an 'any' type.">a</error>, <error descr="TS7006: Parameter 'b' implicitly has an 'any' type.">b</error>) => a * b;

@Component({
  template: `
    <!-- should not report implicit any errors on arrow functions in the template -->
    @let identity = a => a;
    {{ process(identity(1)) }}
  `
})
export class TestComp {

  process(<error descr="TS7006: Parameter 'value' implicitly has an 'any' type.">value</error>) {
    return sum(value, multiply(value, 2));
  }

}
