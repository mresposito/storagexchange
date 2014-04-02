define ([
  "jquery",
  "underscore",
  "backbone",
  "search/elastic",
  "views/post.html"
], function($, _, Backbone, elastic, postHTML) {

  return Backbone.View.extend({

    initialize: function() {
      var posts = elastic.getPosts();
      this.renderPosts(posts);
    },

    renderPosts: function(posts) {
      var $el = this.$el;
      _.map(posts, function(post) {
        $el.find(".content").append(postHTML(post));
      });
    }
  });
});
