import {Component, output} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root (new<caret>Output)="12"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  newOutput = output()
}
