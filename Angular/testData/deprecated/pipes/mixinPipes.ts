// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Pipe, Component, PipeTransform } from '@angular/core';

function formatDate(
  date: Date,
  locale?: string,
  options?: Intl.DateTimeFormatOptions
): string {
  return new Intl.DateTimeFormat(locale, options).format(date);
}

@Pipe({ name: 'formatDate', standalone: true})
export class FormatDate extends fromFunction(formatDate) {}

export function fromFunction<T extends (value: any, ...args: any[]) => any>(
  transform: T
): new () => PipeTransform {
  return class {
    <warning descr="Unused method transform">transform</warning>(...params: Parameters<T>): ReturnType<T> {
      return transform.apply(null, params);
    }
  };
}

@Component({
   standalone: true,
   imports: [FormatDate],
   selector: 'my-app',
   template: `
     <p>{{ date | <error descr="Unresolved pipe formatDte">formatDte</error> }}</p>
     <p>{{ date | formatDate }}</p>
     <p>{{ date | formatDate : 'ru-Ru' }}</p>
     <p>{{ date | formatDate : 'en-En': { dateStyle: 'full' } }}</p>
   `,
   styleUrls: ['./app.component.css']
 })
export class AppComponent {
  date = new Date();
}
