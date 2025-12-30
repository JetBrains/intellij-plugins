import {Component, input} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root [aliasedInput]="12"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  input = input.required({alias: "aliased<caret>Input"})
}
