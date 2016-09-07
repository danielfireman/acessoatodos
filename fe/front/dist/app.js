'use strict';

System.register(['jquery'], function (_export, _context) {
  "use strict";

  var $, App;

  function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
      throw new TypeError("Cannot call a class as a function");
    }
  }

  return {
    setters: [function (_jquery) {
      $ = _jquery.default;
    }],
    execute: function () {
      _export('App', App = function () {
        function App() {
          _classCallCheck(this, App);
        }

        App.prototype.configureRouter = function configureRouter(config, router) {
          config.title = 'Acesso a Todos';
          config.map([{ route: ['', 'welcome'], name: 'welcome', moduleId: 'welcome', nav: true, title: 'Acesso a todos' }]);

          this.router = router;
        };

        App.prototype.attached = function attached() {
          $('#toggles').on('click', function () {
            $('#colMain').toggleClass('col-xs-12 col-xs-9');
            $('#colPush').toggleClass('hidden shown');
          });
        };

        return App;
      }());

      _export('App', App);
    }
  };
});
//# sourceMappingURL=app.js.map
