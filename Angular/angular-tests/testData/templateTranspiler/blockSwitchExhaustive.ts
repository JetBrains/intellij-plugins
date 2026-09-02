import {Component} from '@angular/core';

export interface Loading {
  status: 'loading',
}

export interface Loaded {
  status: 'loaded',
  data: string,
}

export interface Failed {
  status: 'failed',
  error: string,
}

export type State = Loading | Loaded | Failed;

@Component({
  selector: 'non-exhaustive-switch',
  standalone: true,
  template: `
    @switch (state.status) {
      @case ('loading') {
        Loading...
      }
      @case ('loaded') {
        {{ state.data }}
      }
      @default never;
    }
  `
})
export class NonExhaustiveSwitchComponent {
  state!: State
}
