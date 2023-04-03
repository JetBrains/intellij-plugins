import { Component, Input } from '@angular/core';

@Component({
  standalone: true,
  selector: 'app-root',
  templateUrl: './app.component.html',
})
export class AppComponent {

  @Input({required: true})
  requiredInput: String

  @Input()
  notRequiredInput: String

  @Input({alias: "newName"})
  aliasedInput: String

  @Input({alias: "newNameRequired", required: true})
  aliasedRequiredInput: String

  @Input("oldName")
  oldAliasedInput: String

}
