// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, signal, effect} from '@angular/core';
import {createEffect} from "@ngrx/effects";

@Component({
  selector: 'app-root',
  template: ``,
  standalone: true
})
export class AppComponent {

  // Check inspection presence
  readonly <warning descr="Unused readonly field unusedSignal">unusedSignal</warning> = signal(0);
  private <warning descr="Unused field unusedPrivateSignal"><weak_warning descr="TS6133: 'unusedPrivateSignal' is declared but its value is never read.">unusedPrivateSignal</weak_warning></warning> = signal(0);
  <warning descr="Unused method unusedMethod">unusedMethod</warning>(){ }

  // NgRx effects
  private doNothing1$ = createEffect(() => <error descr="TS2322: Type 'null' is not assignable to type 'EffectResult<Action> & \"ActionCreator cannot be dispatched. Did you forget to call the action creator function?\"'.">null</error>);

  //Signal effects
  private loggingEffect = effect(() => null);

  // Lifecycle hooks
  ngOnInit(){ }
  ngOnChanges(){ }
  ngDoCheck(){ }
  ngAfterContentInit(){ }
  ngAfterContentChecked(){ }
  ngAfterViewInit(){ }
  ngAfterViewChecked(){ }
  afterNextRender(){ }
  afterEveryRender(){ }
  ngOnDestroy(){ }
}