define ([
  "jquery",
  "underscore",
  "backbone",
  "views/post.html"
], function($, _, Backbone, postHTML) {

  return Backbone.View.extend({

    events: {
      "change .search": "updateBoard",
      "click a.storageSize": "sizeRange",
      "click .load button": "loadMorePosts"
    },

    initialize: function() {
      this.startingPost = 0;
      this.stepIncrement = 15;
      this.findPosts({});
    },

    loadMorePosts: function() {
      console.log("loading")
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

    checkBottomPage: function(event) {
       $('.container').bind('scroll', function() {
         if($(this).scrollTop() + 
            $(this).innerHeight()
            >= $(this)[0].scrollHeight)
         {
           alert('end reached');
         }
       })
    },

    updateBoard: function() {
      var query = this.queryValue();
      var filters = this.filterValues();
      var starter = this.starterValues();
      var search = _.reduce([query, filters, starter], _.extend);
      this.findPosts(search);
    },

    starterValues: function() {
      return {}
    },

    textSearch: function(event) {
      var json = this.queryValue();
      this.findPosts(json);
    },

    /**
     * Prepare json values
     */
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

    /**
     * Fetch posts from server
     */
    findPosts: function(queries) {
      var self = this;
      $.ajax({
        url:"/api/search/post",
        type: "POST",
        contentType: "application/json",
        data:  JSON.stringify(queries),
        success: function(posts) {
          var data = JSON.parse(posts);
          var hits = data.hits.hits;
          self.renderFacets(data.facets.size.ranges);
          self.renderPosts(hits);
        }
      });
    },

    /**
     * Render function
     */ 
    renderFacets: function(facets) {
      _.map(facets, function (facet) {
        var selector = "ul.storageControls [data-from=" + facet.from + "]"
        var $li = $(selector)
        $li.find(".count").html(facet.count)
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
