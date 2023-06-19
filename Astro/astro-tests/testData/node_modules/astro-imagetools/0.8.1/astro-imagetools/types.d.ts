declare type format =
  | "heic"
  | "heif"
  | "avif"
  | "jpg"
  | "jpeg"
  | "png"
  | "tiff"
  | "webp"
  | "gif";

declare type PotraceOptions = TraceOptions | PosterizeOptions;

declare interface SharedTracingOptions {
  turnPolicy?: "black" | "white" | "left" | "right" | "minority" | "majority";
  turdSize?: number;
  alphaMax?: number;
  optCurve?: boolean;
  optTolerance?: number;
  threshold?: number;
  blackOnWhite?: boolean;
  color?: "auto" | string;
  background?: "transparent" | string;
}

declare interface TraceOptions {
  function?: "trace";
  options?: SharedTracingOptions;
}

declare interface PosterizeOptions {
  function?: "posterize";
  options?: SharedTracingOptions & {
    fill?: "spread" | "dominant" | "median" | "mean";
    ranges?: "auto" | "equal";
    steps?: number | number[];
  };
}

declare interface FormatOptions {
  formatOptions?: Partial<Record<format, ImageToolsConfigs>> & {
    tracedSVG?: PotraceOptions;
  };
}

declare interface PictureFormatOptions extends FormatOptions {
  format?: format | format[] | [] | null;
  fallbackFormat?: format;
  includeSourceFormat?: boolean;
}

declare interface ImgFormatOptions extends FormatOptions {
  format?: format;
}

declare interface ImageToolsConfigs {
  flip?: boolean;
  flop?: boolean;
  invert?: boolean;
  flatten?: boolean;
  normalize?: boolean;
  grayscale?: boolean;
  hue?: number;
  saturation?: number;
  brightness?: number;
  w?: number;
  h?: number;
  ar?: number;
  width?: number;
  height?: number;
  aspect?: number;
  background?: string;
  tint?: string;
  blur?: number | boolean;
  median?: number | boolean;
  rotate?: number;
  quality?: number;
  fit?: "cover" | "contain" | "fill" | "inside" | "outside";
  kernel?: "nearest" | "cubic" | "mitchell" | "lanczos2" | "lanczos3";
  position?:
    | "top"
    | "right top"
    | "right"
    | "right bottom"
    | "bottom"
    | "left bottom"
    | "left"
    | "left top"
    | "north"
    | "northeast"
    | "east"
    | "southeast"
    | "south"
    | "southwest"
    | "west"
    | "northwest"
    | "center"
    | "centre"
    | "cover"
    | "entropy"
    | "attention";
}

declare interface ObjectStyles {
  objectPosition?: string;
  objectFit?: "fill" | "contain" | "cover" | "none" | "scale-down";
}

declare interface BackgroundStyles {
  backgroundPosition?: string;
  backgroundSize?: "fill" | "contain" | "cover" | "none" | "scale-down";
}

declare interface ArtDirective
  extends PrimaryProps,
    ObjectStyles,
    PictureFormatOptions,
    ImageToolsConfigs {
  media: string;
}

declare interface BackgroundImageArtDirective
  extends PrimaryProps,
    BackgroundStyles,
    PictureFormatOptions,
    ImageToolsConfigs {
  media: string;
}

declare type sizesFunction = {
  (breakpoints: number[]): string;
};

declare type breakpointsFunction = {
  (imageWidth: number): number[];
};

declare interface PrimaryProps {
  src: string;
  sizes?: string | sizesFunction;
  placeholder?: "dominantColor" | "blurred" | "tracedSVG" | "none";
  breakpoints?:
    | number[]
    | breakpointsFunction
    | {
        count?: number;
        minWidth?: number;
        maxWidth?: number;
      };
}

declare interface ConfigOptions extends PrimaryProps, ImageToolsConfigs {
  alt: string;
  preload?: format;
  loading?: "lazy" | "eager" | "auto" | null;
  decoding?: "async" | "sync" | "auto" | null;
  layout?: "constrained" | "fixed" | "fullWidth" | "fill";
}

declare interface Attributes {
  container?: Record<any, string>;
  picture?: Record<any, string>;
  style?: Record<any, string>;
  link?: Omit<Record<any, string>, "as" | "rel" | "imagesizes" | "imagesrcset">;
  img?: Omit<
    Record<any, string>,
    | "src"
    | "alt"
    | "srcset"
    | "sizes"
    | "width"
    | "height"
    | "loading"
    | "decoding"
  >;
}

export interface PictureConfigOptions
  extends ConfigOptions,
    ObjectStyles,
    PictureFormatOptions {
  artDirectives?: ArtDirective[];
  attributes?: Omit<Attributes, "container">;
  fadeInTransition?:
    | boolean
    | {
        delay?: string;
        duration?: string;
        timingFunction?: string;
      };
}

export interface ImgConfigOptions
  extends ConfigOptions,
    ObjectStyles,
    ImgFormatOptions {
  attributes?: Omit<Attributes, "picture" | "container">;
}

declare interface BackgroundProps {
  tag?: string;
  content?: string;
}

export interface BackgroundImageConfigOptions
  extends BackgroundProps,
    BackgroundStyles,
    Pick<
      PictureConfigOptions,
      Exclude<
        keyof PictureConfigOptions,
        | "alt"
        | "sizes"
        | "loading"
        | "decoding"
        | "layout"
        | "objectFit"
        | "objectPosition"
        | "artDirective"
        | "fadeInTransition"
      >
    > {
  attributes?: Omit<Attributes, "img" | "picture">;
  artDirectives?: BackgroundImageArtDirective[];
}

export interface BackgroundPictureConfigOptions
  extends BackgroundProps,
    Pick<
      PictureConfigOptions,
      Exclude<keyof PictureConfigOptions, "alt" | "layout">
    > {
  attributes?: Attributes;
}

export interface GlobalConfigOptions
  extends BackgroundStyles,
    Pick<
      PictureConfigOptions,
      Exclude<keyof PictureConfigOptions, "src" | "alt" | "artDirectives">
    > {
  tag?: string;
  cacheDir?: string;
  assetFileNames?: string;
  globalImportRemoteImage?: boolean;
}

declare interface HTMLData {
  link: string;
  style: string;
}

export interface ImageHTMLData extends HTMLData {
  image: string;
}

export interface PictureHTMLData extends HTMLData {
  picture: string;
}

export interface ImgHTMLData extends HTMLData {
  img: string;
}

export interface BackgroundImageHTMLData extends HTMLData {
  htmlElement: string;
}

export type BackgroundPictureHTMLData = BackgroundImageHTMLData;
