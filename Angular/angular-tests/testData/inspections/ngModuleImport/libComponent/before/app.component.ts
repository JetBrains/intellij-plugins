import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <lib-<caret>shared></lib-shared>
  `
})
export class AppComponent {
}
