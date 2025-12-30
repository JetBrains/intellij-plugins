import {Component, output} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root (aliased<caret>Output)="12"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  output = output({alias: "aliasedOutput"})
}
