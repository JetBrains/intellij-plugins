import {
  Component,
  signal,
  computed,
} from '@angular/core';

export interface Movie {
  name: string;
  genre: string;
  releaseYear: number;
  upVote: number;
}

@Component({
             selector: 'app-root',
             template: `{{ movieSig()<caret> }}`,
             standalone: true,
           })
export class AppComponent {
  /** foo */
  movieSig = signal<Movie | null>(null);
}
