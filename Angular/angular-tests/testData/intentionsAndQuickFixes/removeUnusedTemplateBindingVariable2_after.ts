import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
      <div *ngIf="true<caret>"></div>
  `
})
export class AppComponent {
}
