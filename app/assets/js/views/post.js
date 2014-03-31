define ([
  "jquery",
  "underscore",
  "backbone",
  "search/elastic"
], function($, _, Backbone, elastic) {

  return Backbone.View.extend({

    initialize: function() {
      var posts = elastic.getPosts();
      this.renderPosts(posts);
    },

    renderPosts: function(posts) {
      _.map(posts, function(post) {
        console.log(post);
      });
    }
  });
});
