import {<symbolName descr="identifiers//exported function">Component</symbolName>, <symbolName descr="identifiers//exported function">inject</symbolName>} <info descr="null">from</info> '@angular/core';
import { <symbolName descr="identifiers//exported function">signalStore</symbolName>, <symbolName descr="identifiers//exported function">withState</symbolName> } <info descr="null">from</info> '@ngrx/signals';

export interface <symbolName descr="interface">Book</symbolName> {
  <symbolName descr="TypeScript property signature">id</symbolName>: <info descr="null">number</info>;
  <symbolName descr="TypeScript property signature">title</symbolName>: <info descr="null">string</info>;
  <symbolName descr="TypeScript property signature">author</symbolName>: <info descr="null">string</info>;
}

<info descr="null">type</info> <symbolName descr="types//type alias">BookSearchState</symbolName> = {
  <symbolName descr="TypeScript property signature">books</symbolName>: <symbolName descr="interface">Book</symbolName>[];
  <symbolName descr="TypeScript property signature">isLoading</symbolName>: <info descr="null">boolean</info>;
  <symbolName descr="TypeScript property signature">filter</symbolName>: { <symbolName descr="TypeScript property signature">query</symbolName>: <info descr="null">string</info>; <symbolName descr="TypeScript property signature">order</symbolName>: 'asc' | 'desc' };
};

const <symbolName descr="identifiers//local variable">initialState</symbolName>: <symbolName descr="types//type alias">BookSearchState</symbolName> = {
  <symbolName descr="instance field">books</symbolName>: [],
  <symbolName descr="instance field">isLoading</symbolName>: false,
  <symbolName descr="instance field">filter</symbolName>: { <symbolName descr="instance field">query</symbolName>: '', <symbolName descr="instance field">order</symbolName>: 'asc' },
};

export const <symbolName descr="identifiers//exported function">BookSearchStore</symbolName> = <symbolName descr="identifiers//exported function">signalStore</symbolName>(
  // 👇 Providing `BookSearchStore` at the root level.
  { <symbolName descr="instance field">providedIn</symbolName>: 'root' },
  <symbolName descr="identifiers//exported function">withState</symbolName>(<symbolName descr="identifiers//local variable">initialState</symbolName>)
);

<symbolName descr="decorator">@</symbolName><symbolName descr="decorator">Component</symbolName>({
  <symbolName descr="instance field">selector</symbolName>: '<symbolName descr="HTML_TAG_NAME">lib-charts</symbolName>',
  <symbolName descr="instance field">template</symbolName>: `<inject descr="null">
    Books: {{ <symbolName descr="instance field">store</symbolName>.<symbolName descr="ng-signal">books</symbolName>() }}
    Loading: {{ <symbolName descr="instance field">store</symbolName>.<symbolName descr="ng-signal">isLoading</symbolName>() }}
    Loading: {{ <symbolName descr="instance field">store</symbolName>.<error descr="TS2339: Property 'isEmpty' does not exist on type '{ books: Signal<Book[]>; isLoading: Signal<boolean>; filter: DeepSignal<{ query: string; order: \"asc\" | \"desc\"; }>; } & StateSource<{ books: Book[]; isLoading: boolean; filter: { ...; }; }>'.">isEmpty</error>() }}
  </inject>`,
  <symbolName descr="instance field">styles</symbolName>: ``,
  <symbolName descr="instance field">providers</symbolName>: [<symbolName descr="identifiers//exported function">BookSearchStore</symbolName>],
})
export class <symbolName descr="classes//exported class">Charts</symbolName> {
  <info descr="null">readonly</info> <symbolName descr="instance field">store</symbolName> = <symbolName descr="identifiers//exported function">inject</symbolName>(<symbolName descr="identifiers//exported function">BookSearchStore</symbolName>);

  <warning descr="Unused method foo"><symbolName descr="instance method">foo</symbolName></warning>() {
    this.<symbolName descr="instance field">store</symbolName>.<symbolName descr="ng-signal">books</symbolName>()
    this.<symbolName descr="instance field">store</symbolName>.<error descr="TS2339: Property 'isEmpty' does not exist on type '{ books: Signal<Book[]>; isLoading: Signal<boolean>; filter: DeepSignal<{ query: string; order: \"asc\" | \"desc\"; }>; } & StateSource<{ books: Book[]; isLoading: boolean; filter: { ...; }; }>'.">isEmpty</error>()
  }
}
