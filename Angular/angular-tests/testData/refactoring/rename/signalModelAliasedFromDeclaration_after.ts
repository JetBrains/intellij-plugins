import {Component, model} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root [newAliasedModel]="12"/>
    <app-root [(newAliasedModel)]="foo"/>
    <app-root (newAliasedModelChange)="foo"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  model = model(42, {alias: "newAliased<caret>Model"})
}
