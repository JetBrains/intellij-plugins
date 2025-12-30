import {Component, output} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root (the<caret>Output)="12"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  theOutput = output()
}
