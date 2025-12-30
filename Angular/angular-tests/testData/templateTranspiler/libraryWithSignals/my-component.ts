import {Component, input, model, output, signal} from '@angular/core';
import {Test} from 'test';

@Component({
  selector: 'my-component',
  template: `
    <lib-test [inputSignal]="'foo'" [(modelSignal)]="title" (outputSignal)="checkType($event)"></lib-test>
    <lib-test [modelSignal]="title()" (modelSignalChange)="checkType($event)"></lib-test>
    <my-component [inputSignal]="'foo'" [(modelSignal)]="title" (outputSignal)="checkType($event)"></my-component>
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

  checkType(event: string) {
  }
}
