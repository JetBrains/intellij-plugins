import {ErrorHandler, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {IonicApp, IonicErrorHandler, IonicModule} from 'ionic-angular';

@NgModule({
  declarations: [
  ],
  imports: [
    BrowserModule,
    IonicModule.forRoot(ClickerApp),
  ],
  bootstrap: [IonicApp],
  entryComponents: [
  ],
  providers: [
    {provide: ErrorHandler, useClass: IonicErrorHandler},
  ],
})

export class AppModule {}

