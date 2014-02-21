define ([
  "jquery",
  "underscore",
  "backbone"
], function($, _, Backbone) {

  return Backbone.Model.extend({

    defaults: {
      distance: 50
    },
    urlRoot: "/api/v1.0/search/pref"
  });
});
