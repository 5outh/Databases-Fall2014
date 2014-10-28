"""
Access the Google Geocoding API
"""

import requests
import json

class ReverseGeocoder:
    def __init__(self, lat, lng):
        self.lat = lat
        self.lng = lng

    def mkLookupUrl(self):
        baseUrl = 'https://maps.googleapis.com/maps/api/geocode/json?'
        apiKey = 'key=AIzaSyBKdRMMgyQihyY11a2FIwGLmomC35tb6Nc' 
        latlng = 'latlng=' + str(self.lat) + ',' + str(self.lng)
        return baseUrl + latlng + '&' + apiKey

    def lookupPoint(self):
        return requests.get(self.mkLookupUrl()).json()

    def getState(self):
        res = self.lookupPoint()
        for component in res['results'][0]['address_components']:
            types = component['types']
            if ("administrative_area_level_1" in types):
                return component['short_name']

lat, lng = 33.947007, -83.378011

print (json.dumps(ReverseGeocoder(lat,lng).getState(), indent=2))