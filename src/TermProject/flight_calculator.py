import cymysql
from dateutil.parser import parse
from datetime import datetime, timedelta
from math import *

## GLOBALS
FLIGHT_SPEED = 529 # average mph of a commercial flight
HOURLY_PAY = 58.73 # hourly pay at 122,161 / year (average)
DATE_FORMAT = '%I:%M %p - %a %b-%d-%Y'
EARTH_RADIUS = 3958.761 # average radius of earth

## Util functions

def getAirportCodeQuery(apCode):
    return '\n'.join([
        "SELECT lat, lon from flights.airports"
        " WHERE APCode='" + apCode + "'"
        " LIMIT 1"
    ])

def getWaypointForFlightIdQuery(flightId):
    return """
        select *
        from waypoints
        where FlightId = 
        """ + str(flightId)

def parseDate(dateString):
    return datetime.strptime(
        dateString.replace(' (runway)', ''),
        DATE_FORMAT
        )

def getTotalPayForTime(time):
    return (time.total_seconds() / 3600) * HOURLY_PAY

def haversine(p1, p2):
    lat1, lon1 = p1
    lat2, lon2 = p2
    phi_1 = radians(lat1)
    phi_2 = radians(lat2)
    deltaPhi = radians(lat2 - lat1)
    deltaLambda = radians(lon2 - lon1)
    a = sin(deltaPhi/2)**2 + cos(phi_1) * cos(phi_2) * sin(deltaLambda/2)**2
    c = 2 * atan2(sqrt(a), sqrt(1-a))
    return EARTH_RADIUS * c

# Haversine
# formula:    a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
# c = 2 ⋅ atan2( √a, √(1−a) )
# d = R ⋅ c
# where   φ is latitude, λ is longitude, R is earth’s radius (mean radius = 6,371km);
# note that angles need to be in radians to pass to trig functions!

conn = cymysql.connect(
    host='127.0.0.1', 
    user='root', 
    passwd='password', 
    db='flights', 
    charset='utf8'
    )

cur = conn.cursor()

cur.execute(
    """
    select FlightId, dept, dest, time_dept, time_dest
    from flights
    limit 100
    """
    )

flights = [flight for flight in cur.fetchall()]

for flight in flights:
    flightId, dept, dest, time_dept, time_dest = flight
    
    time_dept = parseDate(time_dept)
    time_dest = parseDate(time_dest)

    elapsedTime = time_dest - time_dept
    totalPayFortime = getTotalPayForTime(elapsedTime)

    cur.execute(getAirportCodeQuery(dept))
    deptAirport = [ap for ap in cur.fetchall()][0]

    cur.execute(getAirportCodeQuery(dest))
    destAirport = [ap for ap in cur.fetchall()][0]

    flightDistance = haversine(deptAirport, destAirport)

    cur.execute(getWaypointForFlightIdQuery(flightId))
    waypoints = [waypoint for waypoint in cur.fetchall()]
