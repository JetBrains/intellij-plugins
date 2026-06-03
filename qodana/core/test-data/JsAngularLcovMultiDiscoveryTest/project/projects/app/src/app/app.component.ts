import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `<h1>{{ title() }}</h1>`,
})
export class AppComponent {
  private name = 'fixture';

  title(): string {
    return `Hello, ${this.name}`;
  }

  greet(other: string): string {
    return `Hello, ${other}`;
  }
}
