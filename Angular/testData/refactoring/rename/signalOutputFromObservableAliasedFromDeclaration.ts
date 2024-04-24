import {Component} from '@angular/core';
import {outputFromObservable} from "@angular/core/rxjs-interop";
import {Observable} from "rxjs";

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <app-root (aliasedOutput)="12"/>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  output$ = new Observable<string>()
  output = outputFromObservable(this.output$, {alias: "aliased<caret>Output"})
}
