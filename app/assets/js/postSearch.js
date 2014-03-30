require.config({
  paths: {
    jquery: "/assets/js/jquery-2.0.3.min",
    underscore: "/assets/js/underscore-min",
    backbone: "/assets/js/backbone-min",
    typeahead: "/assets/js/typeahead.min",
    hogan: "/assets/js/hogan-2.0.0",
    elastic: "/assets/js/elasticsearch.min"
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

require ([
  "jquery",
  "underscore",
  "backbone",
  "views/post"
], function($, _, Backbone, PostSearch) {

  new PostSearch({
    el: $(".index")
  });
});
