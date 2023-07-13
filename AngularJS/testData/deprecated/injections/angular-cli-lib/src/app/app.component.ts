import {Component, OnInit} from '@angular/core';
import {Language, LocaleService, TranslationService} from 'my-common-ui-lib';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
  @Language() lang: string;

  title: string;

  constructor(public locale: LocaleService, public translation: TranslationService) { }

  ngOnInit(): void {
    this.translation.translationChanged().subscribe(
      () => { this.title = this.translation.translate('title'); }
    );
  }

  selectLanguage(language: string): void {
    this.locale.setCurrentLanguage(language);
  }
}
