import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
      <div *ngIf="true as if<caret>Unused"></div>
  `
})
export class AppComponent {
}
