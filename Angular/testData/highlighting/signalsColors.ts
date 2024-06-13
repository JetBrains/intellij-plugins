// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {
  <info descr="identifiers//exported function">Component</info>,
  <info descr="identifiers//exported function">computed</info>,
  <info descr="types//type alias">Signal</info>,
  <info descr="identifiers//exported function">signal</info>,
} <info descr="null">from</info> '@angular/core';
import {<info descr="classes//exported class">NgIf</info>} <info descr="null">from</info> "@angular/common";

export interface <info descr="interface">Movie</info> {
  <info descr="TypeScript property signature">name</info>: <info descr="null">string</info>;
  <info descr="TypeScript property signature">genre</info>: <info descr="null">string</info>;
  <info descr="TypeScript property signature">releaseYear</info>: <info descr="null">number</info>;
  <info descr="TypeScript property signature">upVote</info>: <info descr="null">number</info>;
}

<info descr="decorator">@</info><info descr="decorator">Component</info>({
   <info descr="instance field">selector</info>: 'app-root',
   <info descr="instance field">template</info>: `<inject descr="null">
    {{
        this.<info descr="ng-signal">movieSig</info>,
        this.<info descr="ng-signal">newMovieSig</info>(),
        this.<info descr="ng-signal">movieSig</info>,
        this.<info descr="ng-signal">newMovieSig</info>
    }}
    {{
        <info descr="ng-signal">movieSig</info>,
        <info descr="ng-signal">newMovieSig</info>(),
        <info descr="ng-signal">movieSig</info>,
        <info descr="ng-signal">newMovieSig</info>
    }}
    <div *ngIf="<info descr="ng-signal">movieSig</info>() as <info descr="ng-variable">movie</info>">{{<info descr="ng-variable">movie</info>}}</div>

   </inject>`,
  <info descr="instance field">imports</info>:[
    <info descr="classes//exported class">NgIf</info>
  ],
   <info descr="instance field">standalone</info>: true,
 })
export class <info descr="classes//exported class">AppComponent</info> {
  /** foo */
  <info descr="ng-signal">movieSig</info> = <info descr="identifiers//exported function">signal</info><<info descr="interface">Movie</info> | <info descr="null">null</info>>(null);
  <info descr="ng-signal">newMovieSig</info>:<info descr="types//type alias">Signal</info><<info descr="interface">Movie</info> | <info descr="null">null</info>> = <info descr="identifiers//exported function">computed</info>(() => {
    let <info descr="identifiers//local variable">newMovie</info> = {
      <info descr="instance field">name</info>: 'Titanic',
      <info descr="instance field">genre</info>: 'Romance',
      <info descr="instance field">releaseYear</info>: 1997,
      <info descr="instance field">upVote</info>: this.<info descr="ng-signal">movieSig</info>() ?.<info descr="instance field">upVote</info> ?? 0,
    };
    return <info descr="identifiers//local variable">newMovie</info>;
  });
  <warning descr="Unused field withRegularColor"><info descr="instance field">withRegularColor</info></warning>!: <info descr="types//type alias">Record</info><<info descr="null">any</info>,<info descr="null">any</info>>;

  <warning descr="Unused method setMovie"><info descr="instance method">setMovie</info></warning>() {
    this.<info descr="ng-signal">movieSig</info>.<info descr="instance method">set</info>({
      <info descr="instance field">name</info>: 'Spider-Man',
      <info descr="instance field">genre</info>: 'Action, Aventure',
      <info descr="instance field">releaseYear</info>: 2002,
      <info descr="instance field">upVote</info>: 8,
    });
  }

  <warning descr="Unused method updateMovie"><info descr="instance method">updateMovie</info></warning>() {
    this.<info descr="ng-signal">movieSig</info>.<info descr="instance method">update</info>((<info descr="identifiers//parameter">movie</info>) => {
      if (<info descr="identifiers//parameter">movie</info>) <info descr="identifiers//parameter">movie</info>.<info descr="instance field">upVote</info> = <info descr="identifiers//parameter">movie</info>.<info descr="instance field">upVote</info> + 1;
      return <info descr="identifiers//parameter">movie</info>;
    });
  }

  <warning descr="Unused method testMovie"><info descr="instance method">testMovie</info></warning>() {
    this.<info descr="ng-signal">movieSig</info>()
    this.<info descr="ng-signal">newMovieSig</info>()
    this.<info descr="ng-signal">movieSig</info>
    this.<info descr="ng-signal">newMovieSig</info>
  }
}
