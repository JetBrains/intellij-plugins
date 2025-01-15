// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {
  <symbolName descr="identifiers//exported function">Component</symbolName>,
  <symbolName descr="identifiers//exported function">computed</symbolName>,
  <symbolName descr="types//type alias">Signal</symbolName>,
  <symbolName descr="identifiers//exported function">signal</symbolName>,
} <info descr="null">from</info> '@angular/core';
import {<symbolName descr="classes//exported class">NgIf</symbolName>} <info descr="null">from</info> "@angular/common";

export interface <symbolName descr="interface">Movie</symbolName> {
  <symbolName descr="TypeScript property signature">name</symbolName>: <info descr="null">string</info>;
  <symbolName descr="TypeScript property signature">genre</symbolName>: <info descr="null">string</info>;
  <symbolName descr="TypeScript property signature">releaseYear</symbolName>: <info descr="null">number</info>;
  <symbolName descr="TypeScript property signature">upVote</symbolName>: <info descr="null">number</info>;
}

<info descr="decorator">@</info><info descr="decorator">Component</info>({
   <symbolName descr="instance field">selector</symbolName>: '<symbolName textAttributesKey="HTML_TAG_NAME">app-root</symbolName>',
   <symbolName descr="instance field">template</symbolName>: `<inject descr="null">
    {{
        this.<symbolName descr="ng-signal">movieSig</symbolName>,
        this.<symbolName descr="ng-signal">newMovieSig</symbolName>(),
        this.<symbolName descr="ng-signal">movieSig</symbolName>,
        this.<symbolName descr="ng-signal">newMovieSig</symbolName>
    }}
    {{
        <symbolName descr="ng-signal">movieSig</symbolName>,
        <symbolName descr="ng-signal">newMovieSig</symbolName>(),
        <symbolName descr="ng-signal">movieSig</symbolName>,
        <symbolName descr="ng-signal">newMovieSig</symbolName>
    }}
    <div *ngIf="<symbolName descr="ng-signal">movieSig</symbolName>() as <symbolName descr="ng-variable">movie</symbolName>">{{<symbolName descr="ng-variable">movie</symbolName>}}</div>

   </inject>`,
  <symbolName descr="instance field">imports</symbolName>:[
    <symbolName descr="classes//exported class">NgIf</symbolName>
  ],
   <symbolName descr="instance field">standalone</symbolName>: true,
 })
export class <symbolName descr="classes//exported class">AppComponent</symbolName> {
  /** foo */
  <symbolName descr="ng-signal">movieSig</symbolName> = <symbolName descr="identifiers//exported function">signal</symbolName><<symbolName descr="interface">Movie</symbolName> | <info descr="null">null</info>>(null);
  <symbolName descr="ng-signal">newMovieSig</symbolName>:<symbolName descr="types//type alias">Signal</symbolName><<symbolName descr="interface">Movie</symbolName> | <info descr="null">null</info>> = <symbolName descr="identifiers//exported function">computed</symbolName>(() => {
    let <symbolName descr="identifiers//local variable">newMovie</symbolName> = {
      <symbolName descr="instance field">name</symbolName>: 'Titanic',
      <symbolName descr="instance field">genre</symbolName>: 'Romance',
      <symbolName descr="instance field">releaseYear</symbolName>: 1997,
      <symbolName descr="instance field">upVote</symbolName>: this.<symbolName descr="ng-signal">movieSig</symbolName>() ?.<symbolName descr="instance field">upVote</symbolName> ?? 0,
    };
    return <symbolName descr="identifiers//local variable">newMovie</symbolName>;
  });
  <warning descr="Unused field withRegularColor"><symbolName descr="instance field">withRegularColor</symbolName></warning>!: <symbolName descr="types//type alias">Record</symbolName><<info descr="null">any</info>,<info descr="null">any</info>>;

  <warning descr="Unused method setMovie"><symbolName descr="instance method">setMovie</symbolName></warning>() {
    this.<symbolName descr="ng-signal">movieSig</symbolName>.<symbolName descr="instance method">set</symbolName>({
      <symbolName descr="instance field">name</symbolName>: 'Spider-Man',
      <symbolName descr="instance field">genre</symbolName>: 'Action, Aventure',
      <symbolName descr="instance field">releaseYear</symbolName>: 2002,
      <symbolName descr="instance field">upVote</symbolName>: 8,
    });
  }

  <warning descr="Unused method updateMovie"><symbolName descr="instance method">updateMovie</symbolName></warning>() {
    this.<symbolName descr="ng-signal">movieSig</symbolName>.<symbolName descr="instance method">update</symbolName>((<symbolName descr="identifiers//parameter">movie</symbolName>) => {
      if (<symbolName descr="identifiers//parameter">movie</symbolName>) <symbolName descr="identifiers//parameter">movie</symbolName>.<symbolName descr="instance field">upVote</symbolName> = <symbolName descr="identifiers//parameter">movie</symbolName>.<symbolName descr="instance field">upVote</symbolName> + 1;
      return <symbolName descr="identifiers//parameter">movie</symbolName>;
    });
  }

  <warning descr="Unused method testMovie"><symbolName descr="instance method">testMovie</symbolName></warning>() {
    this.<symbolName descr="ng-signal">movieSig</symbolName>()
    this.<symbolName descr="ng-signal">newMovieSig</symbolName>()
    this.<symbolName descr="ng-signal">movieSig</symbolName>
    this.<symbolName descr="ng-signal">newMovieSig</symbolName>
  }
}
