define ([
  "underscore",
  "elastic"
], function(_, Elastic) {

  var client = new Elastic.Client({
    host: 'localhost:9200',
    log: 'trace'
  });

  return {
    getPosts: function() {
      return [{description: "hello", size: "mah"}];
    }
  }
});
