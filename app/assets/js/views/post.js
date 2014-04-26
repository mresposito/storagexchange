define ([
  "jquery",
  "underscore",
  "backbone",
  "views/post.html"
], function($, _, Backbone, postHTML) {

  return Backbone.View.extend({

    events: {
      "change .search": "searchCallback",
      "change .addressSearch": "addressCallback",
      "change input.universitySearch": "uniCallback",
      "click a.storageSize": "sizeRange",
      "click .tt-dropdown-menu": "uniCallback"
    },

    initialize: function() {
      var self = this;
      this.startingPost = 0;
      this.stepIncrement = 15;
      this.findPosts({});
      this.markerArray = [];
      this.circleArray = [];

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

    addressCallback: function() {
      this.startingPost = 0;
      this.updateBoard()
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
      var query = this.queryValue();
      var addr = this.addressValue();
      var filters = this.filterValues();
      var starter = this.starterValues();
      var university = this.universityValues();
      var search = _.extend(query, addr, university, filters, starter);
      this.findPosts(search);
    },

    universityValues: function() {
      var name = this.$el.find(".universitySearch.tt-input").val();
      this.panToLocation(name)
      if(name.length > 2) {
        return {
          university: name
        }
      } else {
        return {}
      }
    },

    panToLocation: function(name) {
      this.clearCircles();
      this.clearMarkers();
      geocoder = new google.maps.Geocoder();
      geocoder.geocode( { 'address': name}, function(results, status) {
        if (status == google.maps.GeocoderStatus.OK) {
          lat = results[0].geometry.location.lat();
          lng = results[0].geometry.location.lng();
          window.map.panTo(new google.maps.LatLng(lat, lng));
          window.map.setZoom(14);
          var circle = new google.maps.Circle({
            center: new google.maps.LatLng(lat, lng),
            radius: 1000,
            fillColor: "#FF0000",
            fillOpacity: 0.35,
            strokeOpacity: 0.0,
            strokeWeight: 0,
            map: window.map
          });
          this.circleArray.push(circle);
          //window.map.fitBounds(circle.getBounds());
        } 
      });
    },

    pinPost: function(post) {
      var latlng = post["_source"].location.split(',');
      var lat = parseInt(arr[0]);
      var lng = parseInt(arr[1]);
      var marker = new google.maps.Marker({
        position: new google.maps.LatLng(lat, lng),
        map: window.map
      });
      this.markerArray.push(marker);
    },

    clearCircles: function() {
      for (var i = 0; i < this.circleArray.length; i++) {
          this.circleArray[i].setMap(null);
      }
      this.circleArray.length = 0;
    },

    clearMarkers: function() {
      for (var i = 0; i < this.markerArray.length; i++) {
          this.markerArray[i].setMap(null);
      }
      this.markerArray.length = 0;
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

    addrSearch: function(event) {
      var json = this.addressValue();
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

    getAddressSearchBox: function() {
      return $(this.el).find(".addressSearch").val();
    },

    addressValue: function() {
      var value = this.getAddressSearchBox()
      if (value) {
        return {
          addressQuery: {
            lat: 40.11374,
            lon: -88.219657
          }
        }
      } else {
        return {};
      }
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
      var $posts = $el.find(".content .posts");
      _.map(posts, this.pinPost);
      _.map(posts, function(post) {
        $posts.append(postHTML(post["_source"]));
      });
    },

    resetBoard: function() {
      $(this.el).find(".content .posts").html("");
    }
  });
});
