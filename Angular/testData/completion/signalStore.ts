import {Component, inject} from '@angular/core';
import { signalStore, withState } from '@ngrx/signals';

export interface Book {
  id: number;
  title: string;
  author: string;
}

type BookSearchState = {
  books: Book[];
  isLoading: boolean;
  filter: { query: string; order: 'asc' | 'desc' };
};

const initialState: BookSearchState = {
  books: [],
  isLoading: false,
  filter: { query: '', order: 'asc' },
};

export const BookSearchStore = signalStore(
  // 👇 Providing `BookSearchStore` at the root level.
  { providedIn: 'root' },
  withState(initialState)
);

const foo = {
  books: []
}

@Component({
  selector: 'lib-charts',
  template: `
    Books: {{ store.<caret> }}
  `,
  styles: ``,
  providers: [BookSearchStore],
})
export class Charts {
  readonly store = inject(BookSearchStore);

  foo() {
    this.store.books()
    this.store.isEmpty()
  }
}
