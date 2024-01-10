import {
  Component,
} from '@angular/core';

export interface Movie {
  name: string;
  genre: string;
  releaseYear: number;
  upVote: number;
}

interface Signal<T> {
  (): T,
  prop: String,
}

@Component({
             selector: 'app-root',
             template: `{{ movieSig<caret> }}`,
             standalone: true,
           })
export class AppComponent {
  /** foo */
  movieSig!: Signal<string>;
}
