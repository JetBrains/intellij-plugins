import {Component, model} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root [newAliased<caret>Model]="12"/>
    <app-root [(newAliasedModel)]="foo"/>
    <app-root (newAliasedModelChange)="foo"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  model = model.required({alias: "newAliasedModel"})
}
