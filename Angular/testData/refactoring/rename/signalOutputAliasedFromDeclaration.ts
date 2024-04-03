import {Component, output} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root (aliasedOutput)="12"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  output = output({alias: "aliased<caret>Output"})
}
