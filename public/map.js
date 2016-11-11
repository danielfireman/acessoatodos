// Note: This example requires that you consent to location sharing when
// prompted by your browser. If you see the error "The Geolocation service
// failed.", it means you probably did not give permission for the browser to
// locate you.
function initMap() {
    var map = new google.maps.Map(document.getElementById('map'), {
      center: {lat: -34.397, lng: 150.644},
      zoom: 16,
      disableDoubleClickZoom: false,
      zoomControl: true,
      scrollwheel: true,
      scaleControl: true,
      streetViewControl: false,
      clickableIcons: false,
      mapTypeControl: false
    });
    // Try HTML5 geolocation.
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(function(position) {
        var pos = {
          lat: position.coords.latitude,
          lng: position.coords.longitude
        };
        map.setCenter(pos);

        $.get(
            "api/v1/nearby?lat=" + position.coords.latitude + "&lng=" + position.coords.longitude,
            "",
            function(data) {
                var markers = [];
                var infoWindows = [];
                for (i = 0; i < data.results.length; i++) {
                    p = data.results[i]
                    console.log(p)
                    markers[i] = new google.maps.Marker({
                        position: new google.maps.LatLng(p.loc.lat, p.loc.lng),
                        title: p.name,
                        map: map,
                        draggable: false
                    });
                    content = '<div id="content">'+
                    '<div id="siteNotice">'+
                    '</div>'+
                    '<h1 id="firstHeading" class="firstHeading">' + p.name + '</h1>'+
                    '<div id="bodyContent">'+
                    '<b>Types:</b>' + p.types + '<br>'
                    if (p.accessibility != null) {
                        content += '<b>Accessibility:</b>' + p.accessibility + '<br>'
                    }
                    content += '</div></div>';
                    infoWindows[i] = new google.maps.InfoWindow({
                        content: content
                    });
                    google.maps.event.addListener(markers[i], 'click', function(pos) {
                        return function() {
                            infoWindows[pos].open(map, markers[pos]);
                        }
                    }(i));
                }
            },
            "json");
      }, function() {
        handleLocationError(true, infoWindow, map.getCenter());
      });
    } else {
      // Browser doesn't support Geolocation
      handleLocationError(false, infoWindow, map.getCenter());
    }
}

function handleLocationError(browserHasGeolocation, infoWindow, pos) {
    infoWindow.setPosition(pos);
    infoWindow.setContent(browserHasGeolocation ?
        'Error: The Geolocation service failed.' :
        'Error: Your browser doesn\'t support geolocation.');
}