import type { UserConfig as ViteUserConfig, UserConfigFn as ViteUserConfigFn } from 'vite';
import type { AstroInlineConfig, AstroUserConfig, Locales, SessionDriverName } from '../types/public/config.js';
/**
 * See the full Astro Configuration API Documentation
 * https://astro.build/config
 */
export declare function defineConfig<const TLocales extends Locales = never, const TDriver extends SessionDriverName = never>(config: AstroUserConfig<TLocales, TDriver>): AstroUserConfig<TLocales, TDriver>;
/**
 * Use Astro to generate a fully resolved Vite config
 */
export declare function getViteConfig(userViteConfig: ViteUserConfig, inlineAstroConfig?: AstroInlineConfig): ViteUserConfigFn;
