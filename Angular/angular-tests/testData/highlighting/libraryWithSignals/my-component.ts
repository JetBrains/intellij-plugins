import {Component, input, model, output, signal} from '@angular/core';
import {Test} from 'test';

@Component({
  selector: 'my-component',
  template: `
    <lib-test [inputSignal]="'foo'" [(modelSignal)]="title" (outputSignal)="checkType(<error descr="TS2345: Argument of type 'void' is not assignable to parameter of type 'string'.">$event</error>)"></lib-test>
    <lib-test [modelSignal]="title()" (modelSignalChange)="checkType($event)"></lib-test>
    <my-component [inputSignal]="'foo'" [(modelSignal)]="title" (outputSignal)="checkType(<error descr="TS2345: Argument of type 'void' is not assignable to parameter of type 'string'.">$event</error>)"></my-component>
    <my-component [modelSignal]="title()" (modelSignalChange)="checkType($event)"></my-component>
  `,
  imports: [
    Test
  ],
})
export class MyComponent {

  title = signal("string")

  modelSignal = model('title');
  inputSignal = input('title');
  outputSignal = output();

  checkType(<warning descr="Unused parameter event"><weak_warning descr="TS6133: 'event' is declared but its value is never read.">event</weak_warning></warning>: string) {
  }
}
