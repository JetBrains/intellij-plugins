import {Component, OnInit} from '@angular/core';
import {TestComponent} from "./test/test.component"

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.component.html',
  imports: [
    TestComponent
  ]
})
export class AppComponent implements OnInit {

}
