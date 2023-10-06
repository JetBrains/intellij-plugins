import {
  Component,
  SIGNAL,
} from '@angular/core';

export interface Movie {
  name: string;
  genre: string;
  releaseYear: number;
  upVote: number;
}

interface MySignal<T> {
  (): T,
  [SIGNAL]: unknown,
  prop: String,
}

@Component({
             selector: 'app-root',
             template: `{{ movieSig()<caret> }}`,
             standalone: true,
           })
export class AppComponent {
  /** foo */
  movieSig!: MySignal<string>;
}
