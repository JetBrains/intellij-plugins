import {Component, input} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root [input]="12"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  in<caret>put = input.required()
}
