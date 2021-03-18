import { Component } from '@angular/core';
import { Person } from './Person';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  titleContent = 'angular-extract-component';
  anotherText = 'foo';
  dedicatedTextInterpolation = 'dedicatedTextInterpolation';

  count = 0;

  actionPrefix = "Action";
  actions = ["a", "b"];
  sections = [1, 2];

  examplePerson: Person = {
    name: "Foomir",
    surname: "McBar",
  };

  handleEvent(event?: Event): void {

  }
}
