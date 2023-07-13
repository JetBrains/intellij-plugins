import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './dest/app.component';
import {App2Component} from "./dest/app2.component"
import {App1Component} from "./dest/app1.component"

@NgModule({
  declarations: [
    AppComponent,
    App1Component,
    App2Component
  ],
  imports: [
    BrowserModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
