import cymysql

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

for flight in flights:
    flightId, dept, dest, time_dept, time_dest = flight
    
    print(flight)

    cur.execute(getAirportCodeQuery(dept))

    deptAirport = [ap for ap in cur.fetchall()][0]

    cur.execute(getAirportCodeQuery(dest))

    destAirport = [ap for ap in cur.fetchall()][0]

    print(deptAirport)
    print(destAirport)

    cur.execute(getWaypointForFlightIdQuery(flightId))

    waypoints = [waypoint for waypoint in cur.fetchall()]

    print(len(waypoints))
