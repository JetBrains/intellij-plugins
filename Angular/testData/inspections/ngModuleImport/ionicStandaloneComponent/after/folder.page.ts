import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {ALL_IONIC_STANDALONE_IMPORTS} from "./ionic.imports";
import {IonContent} from "@ionic/angular/standalone";

@Component({
  selector: 'app-folder',
  templateUrl: './folder.page.html',
  standalone: true,
    imports: [ALL_IONIC_STANDALONE_IMPORTS, IonContent],
})
export class FolderPage implements OnInit {
  public folder!: string;
  private activatedRoute = inject(ActivatedRoute);

  constructor() {
  }

  ngOnInit() {
    this.folder = this.activatedRoute.snapshot.paramMap.get('id') as string;
  }
}
