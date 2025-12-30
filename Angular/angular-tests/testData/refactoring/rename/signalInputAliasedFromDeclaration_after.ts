import {Component, input} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root [newAliasedInput]="12"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  input = input(42, {alias: "newAliased<caret>Input"})
}
