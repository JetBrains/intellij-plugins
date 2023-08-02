import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import {TestComponent} from "./test.component";

@NgModule({
    declarations: [
        AppComponent,
        TestComponent
    ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
