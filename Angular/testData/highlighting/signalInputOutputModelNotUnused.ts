import {Component, input, output, signal, model} from '@angular/core';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  <warning descr="Unused field title">title</warning> = 'untitled46';

  inputWithAlias = input(42, {alias: "aliasedInput"})

  inputWithAlias = input.required({alias: "aliasedInput"})

  modelWithAlias = model(42, {alias: "aliasedModel"})

  modelWithAlias = model.required({alias: "aliasedModel"})

  outputWithAlias = output({alias: "aliasedOutput"})

  <warning descr="Unused field signal">signal</warning> = signal({alias: "aliasedOutput"})
}
