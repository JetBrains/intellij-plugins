// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {
  Component,
  signal,
  computed,
} from '@angular/core';
import {NgIf} from "@angular/common"

export interface Movie {
  name: string;
  genre: string;
  releaseYear: number;
  upVote: number;
}

@Component({
             selector: 'app-root',
             templateUrl: "./signalInGenericStructuralDirective.html",
             imports: [NgIf],
             standalone: true,
           })
export class AppComponent {
  /** foo */
  movieSig = signal<Movie | null>(null);
}
