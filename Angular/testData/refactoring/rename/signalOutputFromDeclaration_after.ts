import {Component, output} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root (newOutput)="12"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  new<caret>Output = output()
}
