import cymysql
from dateutil.parser import parse
from datetime import datetime, timedelta
from math import *
from geocoder import ReverseGeocoder
import random

## GLOBALS
FLIGHT_SPEED = 529 # average mph of a commercial flight
HOURLY_PAY = 58.73 # hourly pay at 122,161 / year (average)
DATE_FORMAT = '%I:%M %p - %a %b-%d-%Y'
EARTH_RADIUS = 3958.761 # average radius of earth
WAYPOINT_NUMBER = 30 # Number of waypoints to place between source and destination

tax_rates = {} # dynamically populated with state -> tax rate mappings

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

def getTaxBracketsForStateQuery(stateCode):
    return "SELECT BracketStart,BracketEnd,IncomeTax FROM flights.taxes WHERE StateCode='" + stateCode + "'"

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

def lerp(p1, p2, num_points):
    x0, y0 = p1
    x1, y1 = p2

    startX = min(x0, x1)
    deltaX = abs(x0 - x1)

    points = []

    for n in range(num_points):
        x = startX + (deltaX / (num_points - 1)) * n
        y = y0 + (y1 - y0) * ((x - x0) / (x1 - x0))
        points.append((x, y))

    return points

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
    totalPayForTime = getTotalPayForTime(elapsedTime)

    cur.execute(getAirportCodeQuery(dept))
    deptAirport = [ap for ap in cur.fetchall()][0]

    cur.execute(getAirportCodeQuery(dest))
    destAirport = [ap for ap in cur.fetchall()][0]

    flightDistance = haversine(deptAirport, destAirport)
    waypoints = lerp(deptAirport, destAirport, WAYPOINT_NUMBER)
    waypointPay = totalPayForTime / WAYPOINT_NUMBER

    totalTax = 0

    for waypoint in waypoints:
        
        reverseGeocoder = ReverseGeocoder(*waypoint)
        stateCode = reverseGeocoder.getState()

        taxRate = 0

        if stateCode in tax_rates:
            taxRate = tax_rates[stateCode]
        else:
            cur.execute(getTaxBracketsForStateQuery(str(stateCode)))
            brackets = list(cur.fetchall())

            # Set the tax rate for the given state for later
            for bracket in brackets:
                lo, hi, rate = bracket
                if 122161 > lo:
                    tax_rates[stateCode] = rate / 100 # Turn percentage into decimal
                    taxRate = rate / 100
        totalTax += taxRate * waypointPay

    # get a random state code from the list of seen states
    randomStateCode = random.choice(list(tax_rates.keys()))
    oldTax = tax_rates[randomStateCode] * totalPayForTime

    print("Old tax for a pilot living in " + str(randomStateCode) + ": " + ("$%.2f" % oldTax))
    print("Old net pay for a pilot living in " + str(randomStateCode) + ": " + ("$%.2f" % (totalPayForTime - oldTax)))

    print("Base pay: " + ("$%.2f" % totalPayForTime))
    print("Total tax: " + ("$%.2f" % totalTax))
    print("Adjusted pay: " + ("$%.2f" % (totalPayForTime - totalTax)))

    print("Tax difference in old taxing policy to new taxing policy: " + ("$%.2f" % (oldTax - totalTax)))