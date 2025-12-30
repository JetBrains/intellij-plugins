// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';

@Component({   
  selector: 'my-appww23',  
  template: `<div [style.font-size]="title ? 'medium' : 'small'"></div>`,
  inputs: ['ae:ea', 'be'],
  outputs: ['ce', 'de']
})       
export class AppComponent {    
  title = 'Tour of Heroes'; 
  heroes = HEROES;
  selectedHero = {firstName: "eee"}
  //added for WebStorm support:
  ae;
  be;
  ce;
  de;
}

