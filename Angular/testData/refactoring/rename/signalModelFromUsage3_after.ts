import {Component, model} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root [newModel]="12"/>
    <app-root [(newModel)]="foo"/>
    <app-root (newModelCh<caret>ange)="foo"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  newModel = model(42)
}
