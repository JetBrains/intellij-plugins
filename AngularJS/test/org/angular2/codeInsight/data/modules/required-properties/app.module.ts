import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { NgIf, NgForOf } from '@angular/common';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports:[
    NgIf, NgForOf
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
