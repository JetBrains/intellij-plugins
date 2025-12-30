import type { EnvironmentProviders } from '@angular/core';
import type { IonicConfig } from '@ionic/core/components';
type OptInAngularFeatures = {
    useSetInputAPI?: boolean;
};
export declare const provideIonicAngular: (config?: IonicConfig & OptInAngularFeatures) => EnvironmentProviders;
export {};
