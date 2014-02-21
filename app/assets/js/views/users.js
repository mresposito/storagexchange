define ([
  "jquery",
  "underscore",
  "backbone",
  "elastic",
  "slider"
], function($, _, Backbone, Elastic, Slider) {

  var client = new Elastic.Client({
    host: 'localhost:9200',
    log: 'trace'
  });

  return Backbone.View.extend({
  });
});
