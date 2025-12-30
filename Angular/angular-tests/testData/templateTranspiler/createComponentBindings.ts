import {
  Component,
  createComponent,
  Directive,
  EnvironmentInjector, EventEmitter, Input,
  inputBinding,
  model, Output, outputBinding,
  signal,
  twoWayBinding
} from '@angular/core';
import {TestBed} from '@angular/core/testing';

@Component({
  selector: 'app-root',
  template: '',
})
export class Comp {
  value = model(12);

  @Input("hostInput")
  input!: string;

  @Output()
  event = new EventEmitter<{value: number}>();
}

@Directive({
  selector: '[foo]'
})
export class Dir {
  value = model(12);

  @Input("dirInput")
  input!: string;

  @Output("the-event")
  event = new EventEmitter<{value: number}>();
}

const events: {value: number}[] = [];
const value = signal('initial');
let dirValue = 'initial';
const hostElement = document.createElement('div');
const environmentInjector = TestBed.inject(EnvironmentInjector);

function createTheComponent() {
  const ref = createComponent(Comp, {
    hostElement,
    environmentInjector,
    directives: [{
      type: Dir,
      bindings: [
        twoWayBinding('value', value),
        inputBinding('dirInput', () => dirValue),
        inputBinding('diInput', () => dirValue),
        outputBinding<{ value: string }>('the-event', (event) => events.push(event)),
        outputBinding<{ value: number }>('events', (event) => events.push(event)),
      ],
    },
    ],
    bindings: [
      inputBinding('hostInput', value),
      inputBinding('hosInput', value),
      twoWayBinding('value', value),
    ],
  });
  return ref
}

