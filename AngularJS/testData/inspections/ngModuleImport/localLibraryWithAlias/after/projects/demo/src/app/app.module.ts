import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {AppComponent} from './app.component';
import {Lib1Module} from "@lib/1";

@NgModule({
  declarations: [
    AppComponent,
  ],
    imports: [
        BrowserModule,
        Lib1Module,
    ],
  bootstrap: [AppComponent]
})
export class AppModule {}
