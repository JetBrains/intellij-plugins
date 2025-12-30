import {Component, input, output, signal, model} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  <warning descr="Unused field title">title</warning> = 'untitled46';

  inputWithAlias = input(42, {alias: "aliasedInput"})

  inputWithAliasRequired = input.required({alias: "aliasedInput"})

  modelWithAlias = model(42, {alias: "aliasedModel"})

  modelWithAliasRequired = model.required({alias: "aliasedModel"})

  outputWithAlias = output({alias: "aliasedOutput"})

  <warning descr="Unused field signal">signal</warning> = signal({alias: "aliasedOutput"})
}
