import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    @let letU<caret>nused = 12;
  `
})
export class AppComponent {
}
