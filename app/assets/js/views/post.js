define ([
  "jquery",
  "underscore",
  "backbone",
  "views/post.html"
], function($, _, Backbone, postHTML) {

  return Backbone.View.extend({

    initialize: function() {
      this.findPosts({});
    },

    findPosts: function(queries) {
      var self = this;
      $.ajax({
        url:"/api/search/post",
        type: "POST",
        contentType: "application/json",
        data:  queries,
        success: function(posts) {
          var hits = posts.hits.hits;
          self.renderPosts(hits);
        }
      });
    },

    renderPosts: function(posts) {
      var $el = $(this.el);
      _.map(posts, function(post) {
        $el.find(".content").append(postHTML(post["_source"]));
      });
    }
  });
});
