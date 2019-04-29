import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {TranslationUiLibModule} from 'my-common-ui-lib';

import {AppComponent} from './app.component';
import {HomeComponent} from './home.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent
  ],
  imports: [
    BrowserModule,
    TranslationUiLibModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
