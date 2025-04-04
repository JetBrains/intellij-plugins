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
  <warning descr="Unused field title">title</warning> = 'ng20next';
  value = model(12);

  @Input("hostInput")
  input!: string;

  @Output()
  event = new EventEmitter<{value: number}>();
}

@Directive()
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

const <warning descr="Unused constant ref"><weak_warning descr="TS6133: 'ref' is declared but its value is never read.">ref</weak_warning></warning> = createComponent(Comp, {
  hostElement,
  environmentInjector,
  directives: [{
      type: Dir,
      bindings: [
        twoWayBinding('<error descr="TS2322: Type 'string' is not assignable to type 'number'.">value</error>', value),
        inputBinding('dirInput', () => dirValue),
        inputBinding('<warning descr="Unrecognized Angular directive input">diInput</warning>', () => dirValue),
        outputBinding<{value: string}>('<error descr="TS2322: Type '{ value: string; }' is not assignable to type '{ value: number; }'.
  Types of property 'value' are incompatible.
    Type 'string' is not assignable to type 'number'.">the-event</error>', (event) => events.push(<error descr="TS2345: Argument of type '{ value: string; }' is not assignable to parameter of type '{ value: number; }'.
  Types of property 'value' are incompatible.
    Type 'string' is not assignable to type 'number'.">event</error>)),
        outputBinding<{value: number}>('<warning descr="Unrecognized Angular directive output">events</warning>', (event) => events.push(event)),
      ],
    },
  ],
  bindings: [
    inputBinding('hostInput', value),
    inputBinding('<warning descr="Unrecognized Angular directive input">hosInput</warning>', value),
    twoWayBinding('<error descr="TS2322: Type 'string' is not assignable to type 'number'.">value</error>', value),
  ],
});

