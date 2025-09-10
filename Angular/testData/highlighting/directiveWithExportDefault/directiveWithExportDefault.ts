import { Component } from '@angular/core';
import TranslateDirective from "./translate.directive"

@Component({
  templateUrl: './directiveWithExportDefault.html',
  imports: [
    TranslateDirective
  ],
})
export class DirectiveWithExportDefault {
  foo: string
}
