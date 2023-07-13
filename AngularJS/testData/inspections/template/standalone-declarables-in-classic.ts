// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {StandaloneComponent, StandaloneDirective, StandalonePipePipe} from "./component";


@Component({
  selector: 'app-root',
  templateUrl: './standalone-declarables-in-classic.html',
})
export class AppComponent {
}

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    BrowserModule,
    StandaloneComponent,
    StandaloneDirective,
    StandalonePipePipe,
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
