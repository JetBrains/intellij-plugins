import {Component, input} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root [in<caret>put]="12"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  input = input(42)
}
