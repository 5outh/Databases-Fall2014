import requests
import json

appID = "8efacea7"

appKey = "ff4a4e0b9806af76726ca8ab5122cded"

url = "https://api.flightstats.com/flex/flightstatus/rest/v2/json/flight/status/AA/100/dep/2014/10/27?appId=" + appID + "&appKey=" + appKey + "&utc=false"

res = requests.get(url).json()

print(json.dumps(res, indent=2))