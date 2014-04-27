define ([
  "jquery",
  "underscore",
  "backbone",
  "views/post.html",
  "views/map"
], function($, _, Backbone, postHTML, Map) {

  return Backbone.View.extend({

    events: {
      "change .search": "searchCallback",
      "change input.universitySearch": "uniCallback",
      "click a.storageSize": "sizeRange",
      "click .tt-dropdown-menu": "uniCallback"
    },

    initialize: function() {
      var self = this;
      this.startingPost = 0;
      this.stepIncrement = 15;
      this.findPosts({});

      Map.initialize()

      $(window).scroll(function () {
        if ($(window).scrollTop() >= $(document).height() - $(window).height()) {
          self.loadMorePosts();
        }
      });
    },

    loadMorePosts: function() {
      this.startingPost += this.stepIncrement;
      this.updateBoard();
    },

    sizeRange: function(event) {
      this.startingPost = 0;
      var link = $(event.target).closest("li");
      if(link.hasClass("active")) {
        link.removeClass("active");
      } else {
        link.addClass("active");
      }
      this.updateBoard();
    },

    searchCallback: function() {
      this.startingPost = 0;
      this.updateBoard()
    },

    uniCallback: function() {
      this.startingPost = 0;
      this.updateBoard()
    },

    updateBoard: function() {
      Map.clearOverlays();
      var query = this.queryValue();
      var filters = this.filterValues();
      var starter = this.starterValues();
      var university = this.universityValues();
      var search = _.extend(query, university, filters, starter);
      this.findPosts(search);
    },

    universityValues: function() {
      var name = this.$el.find(".universitySearch.tt-input").val();
      if(name.length > 2) {
        Map.panToLocation(name)
        return {
          university: name
        }
      } else {
        return {}
      }
    },

    starterValues: function() {
      if(this.startingPost == 0) {
        return {}
      } else {
        return {
          offset: {
            start: this.startingPost,
            limit: 10
          }
        }
      }
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

    getTextSearchBox: function() {
      return $(this.el).find(".search").val();
    },

    queryValue: function() {
      var value = this.getTextSearchBox();
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
        url: "/api/search/post",
        type: "POST",
        contentType: "application/json",
        data:  JSON.stringify(queries),
        success: function(posts) {
          var data = posts;
          try { // when testing, its already parsed in json. very weird
            data = JSON.parse(posts);
          } catch(err) {
            data = posts;
          }
          var hits = data.hits.hits;
          var total = data.hits.total;
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
      // reset only if we are not increasing the posts
      if(this.startingPost == 0) {
        this.resetBoard();
      }
      var $el = $(this.el);
      var $posts = $el.find(".content .posts");
      _.map(posts, Map.pinPost);
      _.map(posts, function(post) {
        $posts.append(postHTML(post["_source"]));
      });
    },

    resetBoard: function() {
      $(this.el).find(".content .posts").html("");
    }
  });
});
