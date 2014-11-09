from html.parser import HTMLParser
from collections import defaultdict
from bs4 import BeautifulSoup
import requests
import json
import xml.etree.ElementTree as ET


planefinder_base_path = "http://planefinder.net"
flightstats_base_path = "http://www.flightstats.com"

def getAirports():
    url = planefinder_base_path + "/data/airports"
    res = requests.get(url)

    soup = BeautifulSoup(res.text)

    countries_accordion = soup.find(id="countries-accordion")

    countries = []
    for country in countries_accordion.find_all("a", attrs={"data-parent" : "#countries-accordion"}):
        countries.append(country)

    temp_airports = {}
    for country in countries:
        name = country.get_text()

        href = country['href']

        # href is an id of form #US where US is the id of the table of airports
        sel = href.replace('#', '')
        table = soup.find(id=sel)

        temp_airports[name] = table

    # airports is a dictionary mapping country names to html tables containing that countries airports
    #   these airports are represented in rows in the table
    #   these rows are of form:
    #       td0 = <a href="/data/airport/FBD" title="Faizbad Airport, Faizbad">Faizbad Airport, Faizbad</a>
    #       td1 = FBD (airport code)
    #       td2 = OAFZ (airport code?)
    #       td3 = 37.1211, 70.5181 (lat, lon)
    #       td4 = 3872ft (altitude)
    airports = []
    for  key, table in temp_airports.items():
        for row in table.find_all("tr"):
            data = row.find_all("td")
        
            if len(data) == 5:
                airport = {}

                anchor = data[0]
                ac0 = data[1].get_text()
                ac1 = data[2].get_text()
                loc = data[3].get_text()
                alt = data[4].get_text()

                #extract lat lon
                split = loc.split(',')
                lat = split[0]
                lon = split[1]

                name = anchor.get_text()

                airport['anchor'] = anchor
                airport['name'] = name
                airport['airportCode'] = ac0
                airport['airportCode1'] = ac1
                airport['lat'] = lat
                airport['lon'] = lon
                airport['alt'] = alt

                airports.append(airport)
    
    return airports

#we can extract flight data for airports from this endpoint:
#   http://www.flightstats.com/go/AirportTracker/airportDeparturesArrivalsUpdate.do?airportCode=atl
#the format is xml
#extracting data from flightracks.com html
def getFlightStatusByAirport(airportCode):
    url = flightstats_base_path + "/go/AirportTracker/airportDeparturesArrivalsUpdate.do?airportCode=" + airportCode

    #res is xml
    res = requests.get(url)

    root = ET.fromstring(res.text)

    flights = []
    for tag in root:
        id = tag.attrib["Id"]
        flight = tag.attrib["Flight"]
        status = tag.attrib["Status"]
        time = tag.attrib["Time"]
        city = tag.attrib["City"]

        flight = {}

        flight['flightId'] = id
        flight['flight'] = flight
        flight['status'] = status
        flight['time'] = time
        flight['city'] = city

        flights.append(flight)
    return flights

def getPositionsForFlight(flightId):
    url = flightstats_base_path + "/go/FlightStatus/flightStatusByFlightPositionDetails.do?id=" + flightId

    res = requests.get(url)

    soup = BeautifulSoup(res.text)

    main = soup.find(id="mainAreaLeftColumn")

    table = main.find_all('table', class_="tableListingTable")

    locations = []
    #table[0] is the table of positions
    if(len(table) > 0):
        table = table[0]

        for tr in table.find_all("tr"):
            td = tr.find_all("td")
                #these tds are of form:
                #   td0 = UTC time
                #   td1 = Time at Departure
                #   td2 = Time t arrival
                #   td3 = speed
                #   td4 = altitude
                #   td5 = latitude
                #   td6 = longitude
            if len(td) == 7:
                location = {}

                location['time'] = td[0].get_text()
                location['dept_time'] = td[1].get_text()
                location['arrival_time'] = td[2].get_text()
                location['speed'] = td[3].get_text()
                location['alt'] = td[4].get_text()
                location['lat'] = td[5].get_text()
                location['lon'] = td[6].get_text()
                locations.append(location)
    
    return locations


#testing
all_statuses = {}
all_positions = {}

print ("getting all airports...")
all_airports = getAirports()

num_airports = len(all_airports)

print ("done getting airports (", len(all_airports), " )")

i = 0;
for airport in all_airports:
    print("Fetching statuses for ", airport['airportCode'] , " ( ", ( (i/num_airports) * 100 ), " )")

    statuses = getFlightStatusByAirport(airport['airportCode'])

    all_statuses[airport['airportCode']] = statuses

    for flight in statuses:
        if(flight['status'] == 'En Route'):
            print ("Fetching positions for flight ", flight['flightId'])
            positions = getPositionsForFlight(flight['flightId'])

            all_positions[flight['flightId']] = positions
    i = i + 1

num_statuses = len(all_statuses)
num_positions = len(all_positions)

print("Total number of airports: ", num_airports)
print("Total number of statuses: ", num_statuses)
print("Total number of positions: ", num_positions)






