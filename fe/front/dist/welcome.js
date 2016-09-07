'use strict';

System.register([], function (_export, _context) {
  "use strict";

  var Welcome;

  function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
      throw new TypeError("Cannot call a class as a function");
    }
  }

  return {
    setters: [],
    execute: function () {
      _export('Welcome', Welcome = function () {
        function Welcome() {
          _classCallCheck(this, Welcome);
        }

        Welcome.prototype.canDeactivate = function canDeactivate() {
          if (this.fullName !== this.previousValue) {
            return confirm('Are you sure you want to leave?');
          }
        };

        return Welcome;
      }());

      _export('Welcome', Welcome);
    }
  };
});
//# sourceMappingURL=welcome.js.map
