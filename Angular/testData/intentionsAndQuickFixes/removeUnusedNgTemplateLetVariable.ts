import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <ng-template let-ngTemp<caret>lateUnused></ng-template
  `
})
export class AppComponent {
}
