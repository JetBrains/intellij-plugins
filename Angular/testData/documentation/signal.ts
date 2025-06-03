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
             template: `{{ newMovie<caret>Sig }}`,
             standalone: true,
           })
export class AppComponent {
  /** foo */
  movieSig = signal<Movie | null>(null);
  /** bar */
  newMovieSig = computed(() => {
    let newMovie = {
      name: 'Titanic',
      genre: 'Romance',
      releaseYear: 1997,
      upVote: this.movieSig()?.upVote,
    };
    return newMovie;
  });
}
