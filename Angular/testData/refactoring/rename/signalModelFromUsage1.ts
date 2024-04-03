import {Component, model} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root [the<caret>Model]="12"/>
    <app-root [(theModel)]="foo"/>
    <app-root (theModelChange)="foo"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  theModel = model(42)
}
