require.config({
  paths: {
    jquery: "/assets/js/jquery-2.0.3.min",
    typeahead: "/assets/js/typeahead.bundle.min"
  },
  shim: {
    jquery: {
      exports: "$"
    },
    typeahead: ["jquery"]
  }
});

require([
  "jquery",
  "views/typeahead"
], function($,Typeahead) {

  Typeahead($('.typeahead'));
});
