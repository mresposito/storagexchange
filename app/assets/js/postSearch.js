require.config({
  paths: {
    jquery: "/assets/js/jquery-2.0.3.min",
    underscore: "/assets/js/underscore-min",
    backbone: "/assets/js/backbone-min",
    typeahead: "/assets/js/typeahead.bundle.min"
  },
  shim: {
    jquery: {
      exports: "$"
    },
    underscore: {
      exports: "_"
    },
    backbone: {
      deps: ["underscore"],
      exports: "Backbone"
    }, 
    typeahead: ["jquery"]
  }
});

require([
  "jquery",
  "underscore",
  "backbone",
  "views/post",
  "views/Typeahead"
], function($, _, Backbone, PostSearch, Typeahead) {

  new PostSearch({
    el: $(".index")
  });
  Typeahead($('.universitySearch'));
});
