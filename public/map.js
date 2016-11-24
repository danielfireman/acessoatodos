// Note: This example requires that you consent to location sharing when
// prompted by your browser. If you see the error "The Geolocation service
// failed.", it means you probably did not give permission for the browser to
// locate you.
function initMap() {
    // ht tps://hpneo.github.io/gmaps/documentation.html
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
                for (var i = 0; i < data.results.length; i++) {
                    var p = data.results[i];
                    var marker = {
                       lat: p.loc.lat,
                       lng: p.loc.lng,
                       title: p.name,
                       infoWindow: {
                         content: getInfoWindowContent(p)
                       },
                    }
                    if (p.accessibility != null && p.accessibility.length > 0) {
                        marker.icon = 'https://maps.google.com/mapfiles/ms/icons/green-dot.png';
                    } else {
                        marker.icon = 'https://maps.google.com/mapfiles/ms/icons/yellow-dot.png';
                    }
                    map.addMarker(marker);
                }
                map.fitZoom();
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

function getInfoWindowContent(p) {
    c = '<div id="content" class="container-fluid">';
    c += '<div class="panel">';
    c += '<div class="panel-heading">';
    c += '<h3 class="panel-title">'+ p.name + '</h3>';
    c += '</div>'; // panel-heading

    c += '<div class="panel-body">';
    c += '<hr>';
    if (p.accessibility != null && p.accessibility.length > 0) {
        c += '<ul class="list-unstyled">';
        if (getChecked(p.accessibility, 1) != "") {
            c += '<li>Rampa de acesso</li>';
        }
        if (getChecked(p.accessibility, 2) != "") {
            c += '<li>Banheiro acessível</li>';
        }
        c += '</ul>';
    } else {
        c += 'Esse lugar é acessível?'
        c += '<br>Contribua com a comunidade clicando no botão abaixo!';
    }
    c += '<hr>';
    c += '<div class="form-actions text-center">';
    c += '<button id="moreinfo" type="button" class="btn btn-info" onclick="onClickMoreInfoBtnFunc(\'' + p.gmplaceid + '\');">';
    c +=  'Mais informações</button>';
    c += '</div>';  // form
    c += '</div>';  // panel-body

    c += '</div>';  // panel
    c += '</div>';  // content
    return c;
}

function onClickMoreInfoBtnFunc(placeID) {
    $.get(
        "api/v1/place/gm::"+placeID,
        "",
        function(p) {
            c  = '<div class="modal fade" role="dialog">';
            c += '<div class="modal-dialog">';
            c += '<div class="modal-content">';

            c += '<div class="modal-header">';
            c += '<button type="button" class="close" data-dismiss="modal">&times;</button>';
            c += '<h4 class="modal-title">' + p.name + '</h4>';
            c += '</div>';  // header

            // Business information.
            c += '<div class="modal-body">';
            c += '<ul class="list-unstyled">';
            if (p.website && p.website != "") {
                c += '<li><a href="' + p.website + ' "target="_blank">Website</a></li>';
            }

            if (p.address && p.address != "") {
                c += '<li>' + p.address + '</li>';
            }
            if (p.phonenumber && p.phonenumber != "") {
                c += '<li>'+ p.phonenumber + '</li>';
            }

            c += '</ul>';
            c += '<hr>';

            // Accessibility form.
            c += '<form role="form">';
            c += '<fieldset>';
            c += '<div class="checkbox checkbox-circle">';
            c += '<input type="checkbox" value="1" '+ getChecked(p.accessibility, 1) +' >';
            c += '<label for="checkbox1">Rampa de acesso</label>';
            c += '</div>';
            c += '<div class="checkbox checkbox-circle">';
            c += '<input type="checkbox" value="2" '+ getChecked(p.accessibility, 2) +'>';
            c += '<label for="checkbox1">Banheiro acessível</label>';
            c += '</div>';
            c += '</fieldset>';
            c += '</form>';
            c += '</div>';  // body

            c += '<div class="modal-footer">';
            c += '<button type="button" class="btn btn-default" data-dismiss="modal" onclick="update(\''+ p.gmplaceid +'\',$(\'input:checkbox:checked\'))">Contribuir!</button>'
            c += '</div>';  // footer
            c += '</div>';  // content
            c += '</div>';  // dialog
            c += '</div>';  // modal
            $(c).modal()
        },
        "json"
    );
}

function update(placeID, acc) {
 $.ajax({
   url: '/api/v1/place/gm::' + placeID,
   type: 'PUT',
   data: JSON.stringify({accessibility:acc.map(function(){return $(this).val();}).get()}),
   success: function(data) {
      window.location = "/";
   }
 });
}

function getChecked(acc, cat) {
  if (!acc || acc.length == 0) {
    return ''
  }
  for (var i = 0; i < acc.length; i++) {
    if (acc[i] == cat) {
       return 'checked=""'
    }
  }
  return ''
}

function handleLocationError(browserHasGeolocation, infoWindow, pos) {
    infoWindow.setPosition(pos);
    infoWindow.setContent(browserHasGeolocation ?
        'Error: The Geolocation service failed.' :
        'Error: Your browser doesn\'t support geolocation.');
}
