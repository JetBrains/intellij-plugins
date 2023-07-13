import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { MyCustomNameModule } from './custom/index';

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    MyCustomNameModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
