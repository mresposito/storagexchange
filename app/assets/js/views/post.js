define ([
  "jquery",
  "underscore",
  "backbone",
  "elastic"
], function($, _, Backbone, Elastic) {

  var client = new Elastic.Client({
    host: 'localhost:9200',
    log: 'trace'
  });

  return Backbone.View.extend({
  });
});
