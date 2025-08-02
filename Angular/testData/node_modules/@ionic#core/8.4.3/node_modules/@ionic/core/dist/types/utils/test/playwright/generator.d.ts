export type Mode = 'ios' | 'md';
export type Direction = 'ltr' | 'rtl';
/**
 * The theme to use for the playwright test.
 *
 * - `light`: The fallback theme values. Theme stylesheet will not be included.
 * - `dark`: The dark theme values.
 * - `high-contrast`: The high contrast light theme values.
 * - `high-contrast-dark`: The high contrast dark theme values.
 */
export type Palette = 'light' | 'dark' | 'high-contrast' | 'high-contrast-dark';
export type TitleFn = (title: string) => string;
export type ScreenshotFn = (fileName: string) => string;
export interface TestConfig {
    mode: Mode;
    direction: Direction;
    palette: Palette;
}
interface TestUtilities {
    title: TitleFn;
    screenshot: ScreenshotFn;
    config: TestConfig;
}
interface TestConfigOption {
    modes?: Mode[];
    directions?: Direction[];
    palettes?: Palette[];
}
/**
 * Given a config generate an array of test variants.
 */
export declare const configs: (testConfig?: TestConfigOption) => TestUtilities[];
export {};
