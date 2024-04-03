import {Component, output} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root (theOutput)="12"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  the<caret>Output = output()
}
