<?php

class FlightTracker extends BaseController {

	public $appID = "8efacea7";
	
	public $appKey = "ff4a4e0b9806af76726ca8ab5122cded";
	
	public $base_tracks_url = "https://api.flightstats.com/flex/flightstatus/rest/v2/json";
	
	public $base_airports_url = "https://api.flightstats.com/flex/airports/rest";
	
	public $base_airlines_url = "https://api.flightstats.com/flex/airlines/rest/v1/json";
	
	protected $time_out = 300; //timeout (in seconds) for the send procedure to last
	
	public function test(){

		$url = "http://planefinder.net/data/airports";
		$result = $this->send($url);
		
		$doc = new DOMDocument();
		
		//$doc->loadHTML($result);
		
		//$saved = $doc->saveHTML();
		
		return $result;
	}
	
	/*********
	*@function	get the status of flights at the airport
	*
	*@param
	*	apcode is the airport code, like JFK
	*	year/month/day are what they sound like
	*	hour is the time of day between 0-23 of the flight
	*	numHours is the range of hours from hour ^ of the flights, default is 1, up to 6
	*	maxFlights is the max number of flights to return
	*
	**********/
	public function flightStatusByAirport($apcode, $year, $month, $day, $hour=12, $numHours=1, $maxFlights=5){
		if($hour > 23 || $hour < 0){
			Logger::make("Error", "airportStatus function call: hour passed is " . $hour . " must be in [0,23]");
			return false;
		}
		if($numHours < 1 || $numHours > 6){
			Logger::make("Error", "airportStatus function call: numHours passed is " . $numHours . " must be in [1,6]");
			return false;
		}
	/*	
		Logger::make("Data", 
			"Requesting airport status for APCode: " . $apcode . " on " . $day . "/" . $month . "/" . $year . 
			" at hour: " . $hour . " with range: +- " . $numHours . 
			" max: " . $maxFlights);
	*/		
		$url = $this->base_tracks_url . "/airport/status/{$apcode}/dep/{$year}/{$month}/{$day}/{$hour}" .
			"?appId={$this->appID}&appKey={$this->appKey}" .
			"&numHours={$numHours}" .
			"&maxFlights={$maxFlights}";
			
		$result = $this->send($url);
		
		return $result;
	}
	
	/*********
	*@function	get the status of flight
	*
	*@param
	*	Flightid is the id given to us by flighttracker
	*		-this id needs to be acquired first by a call to a function that gives the id, like FlightTracker::flightStatusByAirport
	*
	**********/
	public function flightStatus($flightId){
		$url = $this->base_tracks_url . "/flight/status/{$flightId}" .
			"?appId={$this->appID}&appKey={$this->appKey}";
		
		//Logger::make("Data", "Requesting flight status for flight: (flightId) " . $flightId);
		
		$result = $this->send($url);
		
		return $result;
	}
	
	/*********
	*@function	get the tracks (WayPoints) associated with the flight
	*
	*@param
	*	flightId is the id given to us by flighttracker
	*		-this id needs to be acquired first by a call to a function that gives the id, like FlightTracker::flightStatusByAirport
	*	maxPositions is the max number of waypoints
	*
	**********/
	public function flightTracks($flightId, $maxPositions = ""){
		$url = $this->base_tracks_url . "/flight/track/{$flightId}" .
			"?appId={$this->appID}&appKey={$this->appKey}";
		
		if(!empty($maxPositions))
			$url .= "&maxPositions={$maxPositions}";
		
		//Logger::make("Data", "Requesting tracks for flight: (flightId) " . $flightId);
		
		$result = $this->send($url);
		
		return $result;
	}
	
	public function airportsByCountryCode($countryCode = "US"){
		$url = $this->base_airports_url . "/v1/json/countryCode/{$countryCode}?appId={$this->appID}&appKey={$this->appKey}";
		
		$result = $this->send($url);
		
		//put results into an array
		$result = json_decode($result);
		
		$airports = $result->airports;
		
		return $airports;
		
	}
	
	public function allAirlines(){
		$url = $this->base_airlines_url . "/all?appId={$this->appID}&appKey={$this->appKey}";
		
		$result = $this->send($url);
		
		//put results into an array
		$result = json_decode($result);
		
		$airlines = $result->airlines;
		
		return $airlines;
	}
	
	public function send($url){
		set_time_limit($this->time_out);
		
			$ch = curl_init();
			
			curl_setopt ($ch, CURLOPT_URL, $url);
			curl_setopt ($ch, CURLOPT_HEADER, 0);
			curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
			curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, $this->time_out);
			
			$result = curl_exec ($ch);
			curl_close($ch);
		
		
		return $result;
	}
}