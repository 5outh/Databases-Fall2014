import requests
from datetime import *
import json

class FlightTracker:
    def __init__(self, appId, appKey):
        self.appId = appId
        self.appKey = appKey

    def flightStatus(self, carrier, flight, year, month, day):
        url = 'https://api.flightstats.com/flex/flightstatus/rest/v2/json/flight/status/' 
        url += carrier
        url += '/' + flight
        url += '/dep'
        url += '/' + str(year)
        url += '/' + str(month)
        url += '/' + str(day)
        url += ('?appId=' + self.appId + '&appKey=' + self.appKey + '&utc=false')
        return url

    def getFlightStatus(self, carrier, flight, year, month, day):
        url = self.flightStatus(carrier, flight, year, month, day)
        res = requests.get(url)
        return res.json()

    def getFlightStatusNow(self, carrier, flight):
        now = datetime.now()
        return self.getFlightStatus(carrier, flight, now.year, now.month, now.day)

appId = "8efacea7"
appKey = "ff4a4e0b9806af76726ca8ab5122cded"

flightTracker = FlightTracker(appId, appKey)

flightStatus = FlightTracker(appId, appKey).getFlightStatusNow('AA', '100')

print (json.dumps(flightStatus, indent=2))