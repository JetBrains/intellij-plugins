import {Component, input} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root [newInput]="12"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  newInput = input(42)
}
