import {NgModule} from "@angular/core";
import {NgIf} from "./ng_if";
import {NgForOf} from "./ng_for_of";
import {NgPlural, NgPluralCase} from "./ng_plural";
import {ListItemComponent} from "./list-item.component";
import {Dir} from "./dir";

@NgModule({
declarations: [
  Dir,
  NgIf,
  NgForOf,
  NgPlural,
  NgPluralCase,
  ListItemComponent
],
exports: [
  Dir,
  NgIf,
  NgForOf,
  NgPlural,
  NgPluralCase,
  ListItemComponent
]
})
export class Module {
}
