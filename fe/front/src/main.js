import 'bootstrap';

export function configure(aurelia) {
  aurelia.use
    .plugin('aurelia-google-maps', config => {
        config.options({
            apiKey: 'AIzaSyDL66oF-IYUr-DQKjBoV9NXRTFWWyYbhIA',
            apiLibraries: 'drawing,geometry' //get optional libraries like drawing, geometry, ... - comma seperated list
        });
    })
    .standardConfiguration()
    .developmentLogging();

  //Uncomment the line below to enable animation.
  //aurelia.use.plugin('aurelia-animator-css');
  //if the css animator is enabled, add swap-order="after" to all router-view elements

  //Anyone wanting to use HTMLImports to load views, will need to install the following plugin.
  //aurelia.use.plugin('aurelia-html-import-template-loader')

  aurelia.start().then(() => aurelia.setRoot());
}
