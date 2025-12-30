import { ModuleWithProviders } from '@angular/core';
import { StateClass } from '@ngxs/store/internals';
import { NgxsModuleOptions } from './symbols';
import { NgxsRootModule } from './modules/ngxs-root.module';
import { NgxsFeatureModule } from './modules/ngxs-feature.module';
/**
 * Ngxs Module
 */
export declare class NgxsModule {
    private static readonly ROOT_OPTIONS;
    /**
     * Root module factory
     */
    static forRoot(states?: StateClass[], options?: NgxsModuleOptions): ModuleWithProviders<NgxsRootModule>;
    /**
     * Feature module factory
     */
    static forFeature(states?: StateClass[]): ModuleWithProviders<NgxsFeatureModule>;
    private static ngxsTokenProviders;
    private static ngxsConfigFactory;
    private static appBootstrapListenerFactory;
    private static getInitialState;
}
