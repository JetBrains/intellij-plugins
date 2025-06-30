async function getKarmaConfig(env, config) {
    const dev = {
        basePath: '',
        frameworks: ['jasmine'],
        files: [
            './tests/*.js'
        ],
        exclude: [],
        preprocessors: {},
        reporters: ['progress'],
        port: 9876,
        colors: true,
        logLevel: config.LOG_INFO,
        autoWatch: true,
        browsers: ['ChromeHeadless'],
        singleRun: false,
        concurrency: Infinity
    };
    let result;
    if (env === "dev") {
        result = dev;
    }
    return new Promise((resolve) => {
        resolve(result);
    })
}

module.exports = async (config) => {
    const karmaConfig = await getKarmaConfig("dev", config);

    config.set({
        ...karmaConfig
    });
};

