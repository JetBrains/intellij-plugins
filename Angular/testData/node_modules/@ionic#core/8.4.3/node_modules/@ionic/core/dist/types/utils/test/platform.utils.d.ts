export declare const configureBrowser: (config: any, win?: any) => any;
export declare const mockMatchMedia: (media?: string[]) => jest.Mock<any, any, any>;
export declare const PlatformConfiguration: {
    AndroidTablet: {
        navigator: {
            userAgent: string;
        };
        innerWidth: number;
        innerHeight: number;
        matchMedia: jest.Mock<any, any, any>;
    };
    Capacitor: {
        Capacitor: {
            isNative: boolean;
        };
    };
    PWA: {
        navigator: {
            standalone: boolean;
        };
        matchMedia: jest.Mock<any, any, any>;
    };
    Cordova: {
        cordova: boolean;
    };
    DesktopSafari: {
        navigator: {
            userAgent: string;
        };
        innerWidth: number;
        innerHeight: number;
    };
    iPhone: {
        navigator: {
            userAgent: string;
        };
        innerWidth: number;
        innerHeight: number;
        matchMedia: jest.Mock<any, any, any>;
    };
    iPadPro: {
        navigator: {
            userAgent: string;
        };
        innerWidth: number;
        innerHeight: number;
        matchMedia: jest.Mock<any, any, any>;
    };
    Pixel2XL: {
        navigator: {
            userAgent: string;
        };
        innerWidth: number;
        innerHeight: number;
        matchMedia: jest.Mock<any, any, any>;
    };
    GalaxyView: {
        navigator: {
            userAgent: string;
        };
        innerWidth: number;
        innerHeight: number;
        matchMedia: jest.Mock<any, any, any>;
    };
    GalaxyS9Plus: {
        navigator: {
            userAgent: string;
        };
        innerWidth: number;
        innerHeight: number;
        matchMedia: jest.Mock<any, any, any>;
    };
    iPadOS: {
        navigator: {
            userAgent: string;
        };
        innerWidth: number;
        innerHeight: number;
        matchMedia: jest.Mock<any, any, any>;
    };
};
