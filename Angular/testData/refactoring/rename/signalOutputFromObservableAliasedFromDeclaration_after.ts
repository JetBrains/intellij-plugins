import {Component} from '@angular/core';
import {outputFromObservable} from "@angular/core/rxjs-interop";
import {Observable} from "rxjs";

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root (newAliasedOutput)="12"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  output$ = new Observable<string>()
  output = outputFromObservable(this.output$, {alias: "newAliased<caret>Output"})
}
