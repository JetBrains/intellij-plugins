import {Component, model} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root [theModel]="12"/>
    <app-root [(theModel)]="foo"/>
    <app-root (theModelCh<caret>ange)="foo"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  theModel = model(42)
}
