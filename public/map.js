// Note: This example requires that you consent to location sharing when
// prompted by your browser. If you see the error "The Geolocation service
// failed.", it means you probably did not give permission for the browser to
// locate you.
function initMap() {
    // https://hpneo.github.io/gmaps/documentation.html
    var map = new GMaps({
      div: '#map',
      lat: -34.397,
      lng: 150.644,
      zoom: 16
    });
    // Try HTML5 geolocation.
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(function(position) {
        map.setCenter(position.coords.latitude, position.coords.longitude);

        $.get(
            "api/v1/nearby?lat=" + position.coords.latitude + "&lng=" + position.coords.longitude,
            "",
            function(data) {
                for (i = 0; i < data.results.length; i++) {
                    p = data.results[i]
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
                    var marker = {
                       lat: p.loc.lat,
                       lng: p.loc.lng,
                       title: p.name,
                       infoWindow: {
                         content: content
                       }
                    }
                    if (p.accessibility != null) {
                        marker.icon = 'https://maps.google.com/mapfiles/ms/icons/green-dot.png'
                    } else {
                        marker.icon = 'https://maps.google.com/mapfiles/ms/icons/red-dot.png'
                    }
                    map.addMarker(marker);
                    map.fitZoom()
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