// Package placesdb provides an abstraction to the database being used to store data
// about places.
package placesdb

import (
	"fmt"
	"time"

	"gopkg.in/mgo.v2"
	"gopkg.in/mgo.v2/bson"
)

const (
	tableName = "places"
	limit     = 200
)

func Dial(u string, timeout time.Duration) (*Places, error) {
	info, err := mgo.ParseURL(u)
	if err != nil {
		return nil, fmt.Errorf("Error parsing URI:%s err:%q\n", u, err)
	}
	info.Timeout = timeout
	mgoSession, err := mgo.DialWithInfo(info)
	if err != nil {
		return nil, fmt.Errorf("Error connecting to DB:%s err:%q\n", u, err)
	}

	// Reads may not be entirely up-to-date, but they will always see the
	// history of changes moving forward, the data read will be consistent
	// across sequential queries in the same session, and modifications made
	// within the session will be observed in following queries (read-your-writes).
	// http://godoc.org/labix.org/v2/mgo#Session.SetMode
	mgoSession.SetMode(mgo.Monotonic, true)

	dbName := info.Database
	fmt.Printf("Connected to mongo. DB:%s URI:%s\n", dbName, u)
	return &Places{mgoSession, dbName}, nil
}


// A Place object holds the information stored in the database.
type Place struct {
	ID                bson.ObjectId `bson:"_id"`
	GoogleMapsPlaceID string        `bson:"gmplaceid"`
	Location          GeoJson       `bson:"loc"`
	Accessibility     []string      `bson:"acc"`
}

// GeoJson is the format used by the database to encode geographic data structures.
// More information at: http://geojson.org/
type GeoJson struct {
	Type        string    `bson:"type,omitempty"`
	Coordinates []float64 `bson:"coordinates"`
}

// Places is a client library which exports methods to access/change information stored in the
// places database.
type Places struct {
	session *mgo.Session
	dbName  string
}

// NearbySearch synchronously fetches all places within the radius, considering lat and lng the center of a circle.
// radius in expressed in meters.
func (p *Places) NearbySearch(lat, lng, radius float64) ([]Place, error) {
	session := p.session.Copy()
	defer session.Close()
	c := session.DB(p.dbName).C(tableName)
	q := c.Find(bson.M{
		"loc": bson.M{
			"$nearSphere": bson.M{
				"$geometry": bson.M{
					"type":        "Point",
					"coordinates": []float64{lat, lng},
				},
				"$maxDistance": radius,
			},
		},
	})
	var res []Place
	return res, q.Limit(limit).All(&res)
}

// Upsert synchronously upserts the given place.
func (p *Places) Upsert(placeID string, acc []string, lat, lng float64) error {
	objectID, err := p.getObjectID(placeID)
	if err != nil {
		return err
	}

	var place Place
	place.ID = objectID
	place.GoogleMapsPlaceID = placeID
	place.Accessibility = acc
	place.Location = GeoJson{
		Type:        "Point",
		Coordinates: []float64{lat, lng},
	}

	session := p.session.Copy()
	defer session.Close()
	_, err = session.DB(p.dbName).C(tableName).UpsertId(objectID, &place)
	return err
}

// Get synchronously fetches information from the database about a given placeID.
func (p *Places) Get(placeID string) (*Place, error) {
	session := p.session.Copy()
	defer session.Close()
	var place Place
	err := session.DB(p.dbName).C(tableName).Find(bson.M{"gmplaceid": placeID}).One(&place)
	if err != nil && err != mgo.ErrNotFound {
		return nil, err
	}
	return &place, nil
}

// getObjectID synchronously fetches the objectID of the place associated with the given placeID. If the place
// is not stored in the DB, a new ObjectID is created.
func (p *Places) getObjectID(placeID string) (bson.ObjectId, error) {
	session := p.session.Copy()
	defer session.Close()

	var res Place
	err := session.DB(p.dbName).C(tableName).Find(bson.M{"gmplaceid": placeID}).Select(bson.M{"_id": 1}).One(&res)
	switch err {
	case nil:
		return res.ID, nil
	case mgo.ErrNotFound:
		return bson.NewObjectId(), nil
	}
	return "", err
}
