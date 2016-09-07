'use strict';

System.register(['bootstrap'], function (_export, _context) {
    "use strict";

    function configure(aurelia) {
        aurelia.use.plugin('aurelia-google-maps', function (config) {
            config.options({
                apiKey: 'AIzaSyDL66oF-IYUr-DQKjBoV9NXRTFWWyYbhIA',
                apiLibraries: 'drawing,geometry' });
        }).standardConfiguration().developmentLogging();

        aurelia.start().then(function () {
            return aurelia.setRoot();
        });
    }

    _export('configure', configure);

    return {
        setters: [function (_bootstrap) {}],
        execute: function () {}
    };
});
//# sourceMappingURL=main.js.map
