import {Component, input} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root [aliasedIn<caret>put]="12"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  input = input(42, {alias: "aliasedInput"})
}
