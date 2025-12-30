import {Component, model} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root [newAliasedModel]="12"/>
    <app-root [(newAliased<caret>Model)]="foo"/>
    <app-root (newAliasedModelChange)="foo"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  model = model(42, {alias: "newAliasedModel"})
}
