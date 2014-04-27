define ([
  "views/post.html"
], function(postHTML) {
  return {
    initialize: function() {
      if (document.getElementById('map-canvas')) {
          // Coordinates to center the map. U.S by default.
          var latlng = new google.maps.LatLng(37.09024, -95.712891);
       
          // By default center map on U.S.
          var mapOptions = {
            zoom: 3,
            center: latlng,
            mapTypeId: google.maps.MapTypeId.ROADMAP
          };
       
          // Attach a map to the DOM Element, with the defined settings
          window.map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
          // Keep track of circles and markers we place on map
          window.markerArray = [];
          window.circleArray = [];
      }
    },

    panToLocation: function(name) {
      geocoder = new google.maps.Geocoder();
      geocoder.geocode( { 'address': name }, function(results, status) {
        if (status == google.maps.GeocoderStatus.OK) {
          lat = results[0].geometry.location.lat();
          lng = results[0].geometry.location.lng();
          var circle = new google.maps.Circle({
            center: new google.maps.LatLng(lat, lng),
            radius: 3000,
            fillColor: "#FF0000",
            fillOpacity: 0.3,
            strokeOpacity: 0.0,
            strokeWeight: 0,
            map: window.map
          });
          window.circleArray.push(circle);
          window.map.panTo(new google.maps.LatLng(lat, lng));
          window.map.fitBounds(circle.getBounds());
        } 
      });
    },

    pinPost: function(post) {
      if(post["_source"].location !== undefined) {
        var latlng = post["_source"].location.split(',');
        var lat = parseFloat(latlng[0]);
        var lng = parseFloat(latlng[1]);
        var marker = new google.maps.Marker({
          position: new google.maps.LatLng(lat, lng),
          map: window.map
        });
        window.markerArray.push(marker);
      }
    },

    clearOverlays: function() {
      if(window.circleArray !== undefined) {
        for (var i = 0; i < window.circleArray.length; i++) {
          window.circleArray[i].setMap(null);
        }
        window.circleArray.length = 0;
      }

      if(window.markerArray !== undefined) {
        for (var i = 0; i < window.markerArray.length; i++) {
          window.markerArray[i].setMap(null);
        }
        window.markerArray.length = 0;
      }
    }
  };
});
