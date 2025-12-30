import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class Hello1Service {

  ab<caret>c$ = 'ab';

}
