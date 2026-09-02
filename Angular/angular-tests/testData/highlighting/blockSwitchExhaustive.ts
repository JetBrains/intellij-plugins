import {Component} from '@angular/core';

export type State = 'loading' | 'loaded' | 'failed';

@Component({
  selector: 'exhaustive-switch',
  standalone: true,
  template: `
    @switch (state) {
      @case ('loading') {
        Loading...
      }
      @case ('loaded') {
        Loaded
      }
      @case ('failed') {
        Failed
      }
      @default never;
    }
  `
})
export class ExhaustiveSwitchComponent {
  state!: State
}

@Component({
  selector: 'non-exhaustive-switch',
  standalone: true,
  template: `
    @switch (state) {
      @case ('loading') {
        Loading...
      }
      @case ('loaded') {
        Loaded
      }
      <error descr="TS2322: Type '\"failed\"' is not assignable to type 'never'.">@default never</error>;
    }
  `
})
export class NonExhaustiveSwitchComponent {
  state!: State
}
