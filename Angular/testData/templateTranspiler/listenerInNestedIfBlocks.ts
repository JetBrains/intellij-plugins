import {Component, TemplateRef} from '@angular/core';

enum MediaCampo {
  PRINT,
  SCREEN,
}

interface Campo {
  label: string;
  value: string | number | null | string[] | number[] | CampoTemplate;
  fullWidth?: boolean;
  link?: ($event: UIEvent) => void;
  media?: MediaCampo;
}

interface CampoTemplate {
  value: any;
  template: TemplateRef<any>;
  media?: MediaCampo;
}

@Component({
  selector: 'app-root',
  template: `
    @for (item of items; track item) {
      @if (item.media === undefined || item.media === media) {
        @if (!isTemplate(item) && item.link && item.value) {
          <a (click)="item.link($event)">{{ item.value }}</a>
        }
      }
    }
  `,
  styleUrl: './app.css'
})
export class App {
  title = 'idea-undefined-ts-bug';

  media: MediaCampo = MediaCampo.PRINT;

  items: Campo[] = [];

  isTemplate(item: any): item is CampoTemplate {
    return item && item.template instanceof TemplateRef;
  }
}
