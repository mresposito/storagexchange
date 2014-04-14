define ([
  "jquery",
  "underscore",
  "backbone",
  "views/post.html"
], function($, _, Backbone, postHTML) {

  return Backbone.View.extend({

    events: {
      "change .search": "updateBoard",
      "click a.storageSize": "sizeRange"
    },

    initialize: function() {
      this.findPosts({});
    },

    sizeRange: function(event) {
      var link = $(event.target).closest("li");
      if(link.hasClass("active")) {
        link.removeClass("active");
      } else {
        link.addClass("active");
      }
      this.updateBoard();
    },

    updateBoard: function() {
      var query = this.queryValue();
      var filters = this.filterValues();
      this.findPosts(_.extend(query, filters));
    },

    filterValues: function() {
      var $active = $(".storageControls li.active");
      if($active.length == 0) {
        return {};
      } else {
        var posts = _.map($active, function(act) {
          return {
            field: "storageSize",
            gt: $(act).data("from"), 
            lt: $(act).data("to")
          };
        });
        return {filters: posts};
      }
    },

    queryValue: function() {
      var value = $(this.el).find(".search").val();
      if(value.length > 2) {
        return {
          query: {
            term: value
          }
        }
      } else {
        return {};
      }
    },

    textSearch: function(event) {
      var json = this.queryValue();
      this.findPosts(json);
    },

    findPosts: function(queries) {
      var self = this;
      $.ajax({
        url:"/api/search/post",
        type: "POST",
        contentType: "application/json",
        data:  JSON.stringify(queries),
        success: function(posts) {
          var hits = JSON.parse(posts).hits.hits;
          self.renderPosts(hits);
        }
      });
    },

    renderPosts: function(posts) {
      var $el = $(this.el);
      var $posts = $el.find(".content .posts")
      $posts.html("");
      _.map(posts, function(post) {
        $posts.append(postHTML(post["_source"]));
      });
    }
  });
});
