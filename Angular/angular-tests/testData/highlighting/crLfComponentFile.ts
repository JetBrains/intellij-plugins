import {Component, Directive, Input} from '@angular/core';
import { CommonModule } from '@angular/common';

interface FilterTextDate {
  key: string;
  label: string;
  value?: string;
}

export interface FilterText extends FilterTextDate {
  type: "text";
}

export interface FilterDate extends FilterTextDate {
  type: "date";
}

export type Filter = FilterText | FilterDate

@Directive()
export abstract class FilterBaseComponent<T> {
  @Input() filter?: T;
}

@Component({
  selector: 'app-filter-text',
  standalone: true,
  imports: [],
  template: '',
})
export class FilterTextComponent extends FilterBaseComponent<FilterText> {
}


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FilterTextComponent],
  template: `@if (data) {
    @for (filter of data; track filter.key) {
      @switch (filter.type) {
        @case ("text") {
          <app-filter-text [filter]="filter"></app-filter-text>
        }
        @case ("date") {
          <app-filter-text <error descr="TS2322: Type 'FilterDate' is not assignable to type 'FilterText'.
  Types of property 'type' are incompatible.
    Type '\"date\"' is not assignable to type '\"text\"'.">[filter]</error>="filter"></app-filter-text>
          <div *ngFor="let <weak_warning descr="TS6133: 'd' is declared but its value is never read.">d</weak_warning> of <error descr="TS2551: Property 'dafta' does not exist on type 'AppComponent'. Did you mean 'data'?">dafta</error>">
            {{}}
          </div>
        }
      }
    }
  }`,
  styleUrl: './app.component.scss'
})
export class AppComponent {
  <warning descr="Unused field title">title</warning> = 'typing-issue';

  data?: Filter[] | null;
}
