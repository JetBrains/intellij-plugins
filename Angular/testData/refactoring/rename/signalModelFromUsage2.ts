import {Component, model} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root [theModel]="12"/>
    <app-root [(the<caret>Model)]="foo"/>
    <app-root (theModelChange)="foo"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  theModel = model(42)
}
