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
