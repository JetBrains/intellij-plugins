export interface IconDefinition {
    name: string;
    theme?: ThemeType | undefined;
    icon: string;
}
export declare type ThemeType = 'fill' | 'outline' | 'twotone';
export interface Manifest {
    fill: string[];
    outline: string[];
    twotone: string[];
}
export interface CachedIconDefinition {
    name: string;
    theme: string;
    icon: SVGElement;
}
export interface TwoToneColorPaletteSetter {
    primaryColor: string;
    secondaryColor?: string;
}
export interface TwoToneColorPalette extends TwoToneColorPaletteSetter {
    secondaryColor: string;
}
