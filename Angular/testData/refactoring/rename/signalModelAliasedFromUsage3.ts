import {Component, model} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root [aliasedModel]="12"/>
    <app-root [(aliasedModel)]="foo"/>
    <app-root (aliasedModelCh<caret>ange)="foo"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  model = model(42, {alias: "aliasedModel"})
}
