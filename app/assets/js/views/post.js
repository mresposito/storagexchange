define ([
  "jquery",
  "underscore",
  "backbone",
  "views/post.html"
], function($, _, Backbone, postHTML) {

  return Backbone.View.extend({

    events: {
      "change .search": "searchCallback",
      "click a.storageSize": "sizeRange"
    },

    initialize: function() {
      var self = this;
      this.startingPost = 0;
      this.stepIncrement = 15;
      this.findPosts({});

      if (document.getElementById('map-canvas')) {
          // Coordinates to center the map. U.S.
          var latlng = new google.maps.LatLng(37.09024, -95.712891);
       
          // By default center map on U.S.
          var mapOptions = {
            zoom: 3,
            center: latlng,
            mapTypeId: google.maps.MapTypeId.ROADMAP
          };
       
          // Attach a map to the DOM Element, with the defined settings
          window.map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
      }

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

    searchCallback: function() {
      // Test: pan to Chicago after query
      window.map.panTo(new google.maps.LatLng(41.8781, -87.6297));
      window.map.setZoom(14)
      this.startingPost = 0;
      this.updateBoard()
    },

    updateBoard: function() {
      var query = this.queryValue();
      var filters = this.filterValues();
      var starter = this.starterValues();
      var search = _.extend(query, _.extend(filters, starter));
      this.findPosts(search);
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
      var value = this.getTextSearchBox()
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
          self.renderFacets(data.facets.size.ranges);
          self.renderPosts(hits);
          //41.85,-87.65
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
      var $posts = $el.find(".content .posts")
      _.map(posts, function(post) {
        $posts.append(postHTML(post["_source"]));
      });
    },

    resetBoard: function() {
      $(this.el).find(".content .posts").html("");
    }
  });
});
