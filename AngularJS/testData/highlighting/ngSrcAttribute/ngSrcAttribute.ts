// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { ChangeDetectionStrategy, Component } from "@angular/core";
import { NgOptimizedImage } from "@angular/common";
import { XAnchorComponent } from "./components/anchors/x-anchor.component";

@Component({
             changeDetection: ChangeDetectionStrategy.OnPush,
             selector: "app-root",
             standalone: true,
             imports: [NgOptimizedImage, XAnchorComponent],
             template: `
    <div class="relative">
      <div class="pointer-events-none absolute inset-0 h-72">
        <img
          class="object-cover"
          ngSrc="catalogue_background.png"
          fill="true"
          priority="true"
          alt="Catalogue background image"
        />
      </div>
    </div>
  `,
           })
export class AppComponent {}
