import $ from 'jquery';
export class App {
  configureRouter(config, router) {
    config.title = 'Acesso a Todos';
    config.map([
      { route: ['', 'welcome'], name: 'welcome',      moduleId: 'welcome',      nav: true, title: 'Acesso a todos' },

    ]);

    this.router = router;


  }
  attached() {
    $('#toggles').on('click', function () {
        $('#colMain').toggleClass('col-xs-12 col-xs-9');
        $('#colPush').toggleClass('hidden shown');
    });

      }
}
