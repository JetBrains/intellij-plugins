import { Directive, input } from '@angular/core';

@Directive({
  selector: '[jhiTranslate]',
  standalone: true,
})
export default class TranslateDirective {
  readonly jhiTranslate = input.required<string>();
  readonly translateValues = input<Record<string, unknown>>();
}
