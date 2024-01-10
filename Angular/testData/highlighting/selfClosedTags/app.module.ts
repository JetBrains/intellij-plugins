import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { ChildComponent } from './child.component';
import { AppComponent } from "./selfClosedTags"

@NgModule({
  declarations: [
    AppComponent,
    ChildComponent,
  ],
  imports: [
    BrowserModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
