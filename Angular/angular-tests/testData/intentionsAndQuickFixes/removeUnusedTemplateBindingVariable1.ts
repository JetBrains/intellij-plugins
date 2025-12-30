import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <div *ngFor="let for<caret>Unused of [1,2,3];"></div>
  `
})
export class AppComponent {
}
