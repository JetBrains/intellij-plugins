import { IconDefinition, ThemeType } from './types';
export declare const ANT_ICON_ANGULAR_CONSOLE_PREFIX = "[@ant-design/icons-angular]:";
export declare function error(message: string): void;
export declare function warn(message: string): void;
export declare function getSecondaryColor(primaryColor: string): string;
export declare function withSuffix(name: string, theme: ThemeType | undefined): string;
export declare function withSuffixAndColor(name: string, theme: ThemeType, pri: string, sec: string): string;
export declare function mapAbbrToTheme(abbr: string): ThemeType;
export declare function alreadyHasAThemeSuffix(name: string): boolean;
export declare function isIconDefinition(target: string | IconDefinition): target is IconDefinition;
/**
 * Get an `IconDefinition` object from abbreviation type, like `account-book-fill`.
 * @param str
 */
export declare function getIconDefinitionFromAbbr(str: string): IconDefinition;
export declare function cloneSVG(svg: SVGElement): SVGElement;
/**
 * Parse inline SVG string and replace colors with placeholders. For twotone icons only.
 */
export declare function replaceFillColor(raw: string): string;
/**
 * Split a name with namespace in it into a tuple like [ name, namespace ].
 */
export declare function getNameAndNamespace(type: string): [string, string];
export declare function hasNamespace(type: string): boolean;
