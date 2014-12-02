import cymysql
from dateutil.parser import parse
from datetime import datetime, timedelta

## GLOBALS
FLIGHT_SPEED = 529 # average mph of a commercial flight
HOURLY_PAY = 58.73 # hourly pay at 122,161 / year (average)
DATE_FORMAT = '%I:%M %p - %a %b-%d-%Y'

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

def parseDate(dateString):
    return datetime.strptime(
        dateString.replace(' (runway)', ''),
        DATE_FORMAT
        )

flights = [flight for flight in cur.fetchall()]

for flight in flights:
    flightId, dept, dest, time_dept, time_dest = flight
    
    time_dept = parseDate(time_dept)
    time_dest = parseDate(time_dest)

    elapsedTime = time_dest - time_dept

    print(elapsedTime)

    cur.execute(getAirportCodeQuery(dept))

    deptAirport = [ap for ap in cur.fetchall()][0]

    cur.execute(getAirportCodeQuery(dest))

    destAirport = [ap for ap in cur.fetchall()][0]

    print(deptAirport)
    print(destAirport)

    cur.execute(getWaypointForFlightIdQuery(flightId))

    waypoints = [waypoint for waypoint in cur.fetchall()]

    print(len(waypoints))
