import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    @for (item of [1, 2, 3]; track item; let fi<caret>rst = $first, last = $last) {
      {{last}}
    }
  `
})
export class AppComponent {
}
