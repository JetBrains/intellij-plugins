import {Component} from '@angular/core';

export type State = { mode: 'hide' } | { mode: 'show', menu: number };

@Component({
  selector: 'exhaustive-switch-with-expression',
  standalone: true,
  template: `
    @switch (state.mode) {
      @case ('show') {
        {{ state.menu }}
      }
      @case ('hide') {
      }
      @default never(state);
    }
  `
})
export class ExhaustiveSwitchWithExpressionComponent {
  state!: State
}

@Component({
  selector: 'non-exhaustive-switch-with-expression',
  standalone: true,
  template: `
    @switch (state.mode) {
      @case ('show') {
        {{ state.menu }}
      }
      <error descr="TS2322: Type '{ mode: \"hide\"; }' is not assignable to type 'never'.">@default never</error>(state);
    }
  `
})
export class NonExhaustiveSwitchWithExpressionComponent {
  state!: State
}
