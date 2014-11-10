from collections import defaultdict
from bs4 import BeautifulSoup
import requests
import json
import os
import sys
import xml.etree.ElementTree as ET
import datetime

time = datetime.datetime.now().time()
print ("called at " + str(time))

cwd = os.getcwd()
fn = cwd + "/flight.log"
f = open(fn, "w+")
f.write("call to flight_scraper.py\n")
f.close()

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

                airport['countryCode'] = anchor['href'].replace('#', '')
                airport['name'] = name
                airport['airportCode'] = ac0
                airport['airportCode1'] = ac1
                airport['lat'] = lat
                airport['lon'] = lon
                airport['alt'] = alt

                airports.append(airport)
    
    return airports
    
def getAirportsByCountry(countryCode):
    airports = []
    url = planefinder_base_path + "/data/airports"
    res = requests.get(url)

    soup = BeautifulSoup(res.text)

    countries_accordion = soup.find(id="countries-accordion")

    for country in countries_accordion.find_all("a", attrs={"data-parent" : "#countries-accordion"}):
        name = country.get_text()

        href = country['href']

        # href is an id of form #US where US is the id of the table of airports
        sel = href.replace('#', '')

        if countryCode in sel:
            table = soup.find(id=sel)

            # airports is a dictionary mapping country names to html tables containing that countries airports
            #   these airports are represented in rows in the table
            #   these rows are of form:
            #       td0 = <a href="/data/airport/FBD" title="Faizbad Airport, Faizbad">Faizbad Airport, Faizbad</a>
            #       td1 = FBD (airport code)
            #       td2 = OAFZ (airport code?)
            #       td3 = 37.1211, 70.5181 (lat, lon)
            #       td4 = 3872ft (altitude)    
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

                    airport['countryCode'] = sel
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
    headers = {
    	'User-Agent' : 'Fuck you', #'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/537.36',
    	'Connection' : 'keep-alive',
    	'Host' : 'www.flightstats.com',
    	'Referer' : 'https://www.google.com',
    	'Cache-Control' : 'max-age=0',
    	'Accept' : 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
    	'Accept-Encoding' : 'gzip, deflate, sdch',
    	'Accept-Language' : 'en-US,en;q=0.8'
    }
    
    cookies = {
    	"JSESSIONID" : "8B2C6934B7410CD7DC36F9E6FD750D45.web2:8009",
    	"FS_tokenIQL" : "8B2C6934B7410CD7DC36F9E6FD750D45.web2:8009",
    	 "__qca" : "P0-78351524-1415626474438",
    	 "__gads" : "ID=e710aef40ab1c21f:T=1415626474:S=ALNI_MaHm-QE3mKfIBTqZRYdjX-CfyB4gA",
    	 "__utma" : "104620247.1741206257.1415626474.1415626474.1415626474.1",
    	 "__utmb" : "104620247.24.7.1415626733128",
    	 "__utmc" : "104620247",
    	 "__utmz" : "104620247.1415626474.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)"
    }
    print ("getting positions for flightId: " + flightId);
    url = flightstats_base_path + "/go/FlightStatus/flightStatusByFlightPositionDetails.do?id=" + flightId

    res = requests.get(url, headers = headers, cookies=cookies)
    print (res.request.headers)
    print (res.text)
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
filename = sys.argv[1]
file = open(filename, "w+")

results = {}
all_statuses = {}
all_positions = {}

us_airports = getAirportsByCountry('US')

results['airports'] = us_airports

num_airports = len(us_airports)

print ("done getting airports (", len(us_airports), " )")

i = 0;
for airport in us_airports:
    print("Fetching statuses for ", airport['airportCode'] , " ( ", ( (i/num_airports) * 100 ), " )")

    statuses = getFlightStatusByAirport(airport['airportCode'])

    all_statuses[airport['airportCode']] = statuses

    for flight in statuses:
        if(flight['status'] == 'En Route'):
           # print ("Fetching positions for flight ", flight['flightId'])
            positions = getPositionsForFlight(flight['flightId'])

            all_positions[flight['flightId']] = positions
    i = i + 1

num_statuses = len(all_statuses)
num_positions = len(all_positions)

results['statuses'] = all_statuses
results['positions'] = all_positions
print("Total number of airports: ", num_airports)
print("Total number of statuses: ", num_statuses)
print("Total number of positions: ", num_positions)


json.dump(results, file)
file.close()





