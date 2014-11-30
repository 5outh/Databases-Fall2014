<?php

//This class provides an interface between the data given to us by flightracks
//and the data format that the eloquent orm expects when we are creating database entries.

class DataController extends BaseController {
	/***
	*@function
	*	helper function to update stuff
	*@param $what
	*	what to update, like 'airports'
	*
	*/
	public function update($what){
		$flighttracker = new FlightTracker;
		
		$_SESSION['created'] = array(
			'Airport' => 0,
			'State' => 0,
			'City' => 0,
			'Airline' => 0,
			'Pay' => 0,
			'Tax' => 0,
			'Flight' => 0,
			'Result' => 0,
			'WayPoint' => 0,
			'AirPlane' => 0,
		);
				
		$_SESSION['updated'] = array(
			'Airport' => 0,
			'State' => 0,
			'City' => 0,
			'Airline' => 0,
			'Pay' => 0,
			'Tax' => 0,
			'Flight' => 0,
			'Result' => 0,
			'WayPoint' => 0,
			'AirPlane' => 0
		);
		$what = strtolower($what);
		switch($what){
			case "all":
				DataController::dailyUpdate();
				
				$messages = makeMessages();
				
				return Redirect::to("admin")->with("messages", $messages);
			case "airports":
				$airports = $flighttracker->airportsByCountryCode("US");
				
				DataController::batchAirports((array)$airports);
				
				$messages = makeMessages();
				
				return Redirect::to('admin')->with("messages", $messages);
			case "airlines":
				$airlines = $flighttracker->allAirlines();
				
				$airline = DataController::batchAirlines((array)$airlines);
				
				$messages = makeMessages();
				
				return Redirect::to('admin')->with('messages', $messages);
			case "flights":
				$state = Input::get("state"); //this is the state we need to get the airports for
			
				DataController::dailyFlightUpdateByState($state);
				
				$messages = makeMessages();
				
				return Redirect::to("admin")->with("messages", $messages);
			case "tracks":
			
			default:
				return Redirect::back()->with("messages", array("Did not recognize that"));
		}
	}
	
	//update all flights in America for today
	public static function dailyUpdate(){
		$states = State::all();
		
		$start = microtime(true);
		
		Logger::make("Data", "Call to dailyUpdate() at " . $start);
		
		foreach($states as $state){
			DataController::dailyFlightUpdateByState($state['StateCode']);
		}
		
		$stop = microtime(true);
		$elapsed = $stop - $start;
		
		Logger::make("Debug", "Call to dailyUpdate() start: {$start}; stop: {$stop}; elapsed: {$elapsed};");
		
		$messages = makeMessages();
		foreach($messages as $message)
			Logger::create("Data", "dailyUpdate() - " . $message);
	}
	
	//update all flights departing from all airports in this state today
	//ex: if AZ has 15 airports, and each airport had 1000 flights today, then this function will attempt to update 15,000 flights (and maybe additional fields)
	public static function dailyFlightUpdateByState($stateCode){
		$ft = new FlightTracker;
		
		$airports = Airport::where("StateCode", "=", $stateCode)->get();
		
		$year = date("Y");
		$month = date("m");
		$day = date("j");
		
		Logger::make("Data", "Call to dailyFlightUpdateByState. state : " . $stateCode . ", " . count($airports) . " airports, ". $day . "/" . $month . "/". $year);
		
		//debuggin
		$start = microtime(true);
		foreach($airports as $airport){
			
			$apcode = $airport['APCode'];
			
			$times = array();
			
			$result0 = $ft->flightStatusByAirport($apcode, $year, $month, $day, 0, 6);
			$result1 = $ft->flightStatusByAirport($apcode, $year, $month, $day, 6, 6);
			$result2 = $ft->flightStatusByAirport($apcode, $year, $month, $day, 12, 6);
			$result3 = $ft->flightStatusByAirport($apcode, $year, $month, $day, 18, 6);
			
			$times[] = DataController::extractResponse($result0);
			$times[] = DataController::extractResponse($result1);
			$times[] = DataController::extractResponse($result2);
			$times[] = DataController::extractResponse($result3);
			
			foreach($times as $time)
				DataController::massCreate($time);
		}
		$stop = microtime(true);
		$elapsed = $stop - $start;
		Logger::make("Debug", count($airports) . " airports have ( " . $_SESSION['created']['Flight'] . " ) flights . start: " . $start . "; stop: " . $stop . "; elapsed: " . $elapsed . ";");
					
	}
	
	//use this function to create a bunch of different entities formatted into an array by a call to extractResponse();
	public static function massCreate($entities){
		if(isset($entities['airports'])){
			foreach($entities['airports'] as $airport)
				DataController::newOrUpdate("Airport", $airport);
		}
		
		if(isset($entities['flightStatuses'])){
			foreach($entities['flightStatuses'] as $flight)
				DataController::newOrUpdate("Flight", $flight);
		}
		
		if(isset($entities['airlines'])){
			foreach($entities['airlines'] as $airline)
				DataController::newOrUpdate("Airline", $airline);
		}
		
		if(isset($entities['equipments'])){
			foreach($entities['equipments'] as $airplane)
				DataController::newOrUpdate("AirPlane", $airplane);
		}
		
		if(isset($entities['locations'])){
			foreach($entities['locations'] as $waypoint)
				DataController::newOrUpdate("WayPoint", $waypoint);
		}
	}
	//create airports from an array
	public static function batchAirports($airports){
		foreach($airports as $airport){
			DataController::createAirport((array)$airport);
		}
	}	
	
	//create a single airport
	public static function createAirport($data){
		$geo = new Geolocator;
		
		$airport = DataController::extractAirport($data);
		$city = DataController::extractCity($data);
		$state = DataController::extractState($data);
		
		$cityData = $geo->cityInfo($city['CityName'], $state['StateCode']);
		
		if($cityData)
			$city = fuse($city, $cityData);
		
		$state = DataController::newOrUpdate("State", $state);
		$city = DataController::newOrUpdate("City", $city);
		$airport = DataController::newOrUpdate("Airport", $airport);
	}
	
	//create airlines from an array
	public static function batchAirlines($airlines){
		foreach($airlines as $airline){
			DataController::createAirline((array)$airline);
		}
	}
	
	//create a single airline
	public static function createAirline($airline){
		$airline = DataController::extractAirline($airline);
		
		$airline = DataController::newOrUpdate("Airline", $airline);
		
		return $airline;
	}
	
	public static function extractAirport($data){
		$fields = array();
		
		$fields["APCode"] 	= 	$data['fs'];
		$fields["lat"] 		= 	isset($data['latitude']) 	? $data['latitude'] 	: '-';
		$fields["lon"] 		= 	isset($data['longitude']) 	? $data['longitude'] 	: '-';
		$fields["AirportName"] 	= 	isset($data['name']) 		? $data['name'] 	: '-';
		$fields["Street1"] 	= 	isset($data['street1']) 	? $data['street1'] 	: '-';
		$fields["Street2"] 	= 	isset($data['street2']) 	? $data['street2'] 	: '-';
		$fields["Zip"] 		= 	isset($data['postalCode']) 	? $data['postalCode'] 	: '-';
		$fields["CityCode"] 	= 	isset($data['cityCode']) 	? $data['cityCode'] 	: '-';
		$fields["StateCode"] 	= 	isset($data['stateCode']) 	? $data['stateCode'] 	: '-';
		
		return $fields;
	}
	
	public static function extractCity($data){
		$city = array();
		
		$city['CityCode'] = isset($data['cityCode']) 	? $data['cityCode'] 	: '-';
		$city['CityName'] = isset($data['city'])	? $data['city']		: '-';
		$city['StateCode'] = isset($data['stateCode'])	? $data['stateCode']	: '-';
		
		return $city;
	}
	
	public static function extractState($data){
		$state = array();
		
		$state['StateCode'] = 	isset($data['stateCode']) ? $data['stateCode'] : '-';
		
		return $state;
	}
	
	public static function extractAirline($data){
		$a = array();
		
		$a['ALCode'] = $data['fs'];
		$a['AirlineName'] = isset($data['name']) ? $data['name'] : '-';
		
		return $a;
	}
	
	public static function extractFlight($data){
		$a = array();
		
		$a['FlightId'] 		= 	$data['flightId'];
		$a['FlightNumber']	=	isset($data['flightNumber'])		? $data['flightNumber'] 	: '-';
		$a['dept']		=	isset($data['departureAirportFsCode'])	? $data['departureAirportFsCode'] : '-';
		$a['dest']		=	isset($data['arrivalAirportFsCode'])	? $data['arrivalAirportFsCode'] : '-';
		$a['time_dept']		=	isset($data['departureDate']->dateUtc)	? $data['departureDate']->dateUtc : '-';
		$a['time_dest']		=	isset($data['arrivalDate']->dateUtc)	? $data['arrivalDate']->dateUtc : '-';
		$a['Status']		=	isset($data['status'])			? $data['status'] : '-';
		$a['AirPlaneCode']	=	isset($data['flightEquipment']->actualEquipmentIataCode) ? $data['flightEquipment']->actualEquipmentIataCode : '-';
		$a['ALCode']		= 	isset($data['carrierFsCode']) 		? $data['carrierFsCode'] : '-';
		
		return $a;
	}
	
	public static function extractAirplane($data){
		$a = array();
		
		$a['AirPlaneCode'] 	= 	isset($data['iata'])	?	$data['iata']	:	'-';
		$a['AirPlaneName']	=	isset($data['name'])	? 	$data['name'] 	: 	'-';
		
		return $a;
	}
	
	public static function extractWaypoint($data){
		$a = array();
		
		$a['FlightId'] 		= 	isset($data['flightId'])	?	$data['flightId']	:	'-';
		$a['lat']		=	isset($data['lat'])		? 	$data['lat'] 		: 	'-';
		$a['lon']		=	isset($data['lon'])		? 	$data['lon'] 		: 	'-';
		$a['Speed']		=	isset($data['speedMph'])	?	$data['speedMph']	:	'-';
		$a['Altitude']		=	isset($data['altitudeFt'])	?	$data['altitudeFt']	:	'-';
		$a['Date']		=	isset($data['date'])		?	$data['date']		:	'-';
		
		return $a;
	}
	
	//handle a response from a request to flightracker with multiple entities
	public static function extractResponse($data){
		$a = array();
		
		$data = json_decode($data);
		
		if(isset($data->error)){
			Logger::make("Error", "Error in response. errorId: {$data->error->errorId}; errorMessage: {$data->error->errorMessage}");
		}
		
		$request = $data->request;
		$appendix =(array) $data->appendix;
		
		if(isset($appendix['airports'])){
			$airports = $appendix['airports'];
			
			$a['airports'] = array();
			
			foreach($airports as $airport){
				$a['airports'][] = DataController::extractAirport((array)$airport);
			}
		}
		
		if(isset($appendix['airlines'])){
			$airlines = $appendix['airlines'];
			
			$a['airlines'] = array();
			foreach($airlines as $airline){
				$a['airlines'][] = DataController::extractAirline((array)$airline);
			}
		}
		
		if(isset($appendix['equipments'])){
			$equipment = $appendix['equipments'];

			$a['equipments'] = array();
			
			foreach($equipment as $plane){
				$a['equipments'][] = DataController::extractAirplane((array)$plane);
			}
		}
		
		if(isset($data->flightStatuses)){
			$statuses = $data->flightStatuses;
			
			$a['flightStatuses'] = array();
			
			foreach($statuses as $status){
				$a['flightStatuses'][] = DataController::extractFlight((array)$status);
			}
		}
		
		if(isset($data->flightTracks)){
			
			if(!isset($a['flightStatuses']))
				$a['flightStatuses'] = array();
				
			foreach($data->flightTracks as $flight){
				$a['flightStatuses'][] = DataController::extractFlight((array)$flight);
				
				//extract positions
				if(isset($flight->positions)){
					
					if(!isset($a['positions']))
						$a['positions'] = array();
						
					foreach($flight->positions as $waypoint){
						$waypoint->flightId = $flight->flightId;
						
						$a['positions'][] = DataController::extractWaypoint((array)$waypoint);
					}
				}
			}
		}
		
		if(isset($data->flightTrack)){
			
			if(!isset($a['flightStatuses']))
				$a['flightStatuses'] = array();
				
			$a['flightStatuses'][] = DataController::extractFlight((array)$data->flightTrack);
				
			//extract positions
			if(isset($data->flightTrack->positions)){
					
				if(!isset($a['positions']))
					$a['positions'] = array();
						
				foreach($data->flightTrack->positions as $waypoint){
					$waypoint->flightId = $data->flightTrack->flightId;
					
					$a['positions'][] = DataController::extractWaypoint((array)$waypoint);
				}
			}
		}
		
		return $a;
	}
	
	public static function newOrUpdate($type, $values){
		$info = DataController::getIdentifier($type, $values);
		
		$key = $info['key'];
		$value = $info['value'];
		$model = $info['model'];
		
		$thing = $model::where($key, "=", $value)->first(); 
		
		if(empty($thing)){
			$new = $model::create($values);
			$_SESSION['created'][$type]++;
			return $new;
		}

		foreach($values as $key => $value){
			$thing->$key = $value;
		}
		
		$thing->save();
		$_SESSION['updated'][$type]++;
		return $thing;
	}
	
	public static function newOrNothing($type, $values){
		$info = DataController::getIdentifier($type, $values);
		
		$key = $info['key'];
		$value = $info['value'];
		$model = $info['model'];
		
		$thing = $model::where($key, '=', $value);
		
		if(empty($thing)){
			$new = $model::create($values);
			$_SESSION['created'][$type]++;
			return $new;
		}
		
		return $thing;
	}
	
	public static function getIdentifier($type, $values){
		$key = "";
		$value = ""; 
		$model = "";
		
		$response = array(
			"key" => $key,
			"value" => $value,
			"model" => $model
		);
		
		switch($type){
			case "Airport":
				$key = "APCode";
				$value = $values['APCode'];
				$model = new Airport;
				break; 
			case "State":
				$key = "StateCode";
				$value = $values['StateCode'];
				$model = new State;
				break; 
			case "City":
				$key = "CityCode";
				$value = $values['CityCode'];
				$model = new City;
				break; 
			case "Airline":
				$key = "ALCode";
				$value = $values['ALCode'];
				$model = new Airline;
				break; 
			case "Flight":
				$key = "FlightId";
				$value = $values['FlightId'];
				$model = new Flight;
				break; 
			case "AirPlane":
				$key = "AirPlaneCode";
				$value = $values['AirPlaneCode'];
				$model = new AirPlane;
				break; 
			case "PilotPay":
				$key = "id";
				$value = $values['id'];
				$model = new Pay;
				break; 
			case "Tax":
				$key = "id";
				$value = $values['id'];
				$model = new Tax;
				break; 
		}
		
		$response['key'] = $key;
		$response['value'] = $value;
		$response['model'] = $model;
		
		return $response;
	}
	
	//make a table with the type
	public function view($type){
		$data = $type::all();
		
		$model = new $type;
		return View::make('table')
		->with("attributes", $model->getAttributes())
		->with("data", $data);
	}
	
	
	//use for debugging
	//result @ /data/test
	public function test(){
		$ft = new FlightTracker;
		
		$atlStatus = $ft->flightStatusByAirport("ATL", 2014, 11, 6, 0, 1, 10);
		
		$extracted = DataController::extractResponse($atlStatus);
		echo "<h1>Extracted</h1>";
		$this->test_printExtracted($extracted);
	
		if(isset($extracted['flightStatuses'])){
			$tracks = array();
			foreach($extracted['flightStatuses'] as $flight){
				$tracks[] = DataController::extractResponse($ft->flightTracks($flight['FlightId']));
				
			}
		}
		
		echo "<br><br><br><h1>Extracted tracks:</h1>";
		foreach($tracks as $track)	$this->test_printExtracted($track);
	}
	
	public function test_printExtracted($extracted){
		foreach($extracted as $key=>$value){
			echo "<h3>".$key." (".count($extracted[$key]).")</h3>";
			for($i=0; $i<count($extracted[$key]); $i++){
				echo "<p>{$i}</p>";
				print_r($extracted[$key][$i]);
			}
		}
	}
	
	public function delete($what){
		switch($what){
			case "Logger":
				Logger::truncate();
				return Redirect::back()->with("messages", array("Deleted logs"));
			default:
				return Redirect::back()->with("messages", array("Probably don't want to do that"));
		}
	}
}

//helper function, acts as ternary, either return the value or the default one
function v($val, $def){
	return isset($val) ? $val : $def;
}

//helper function
//left gets anything that is in right AND not in left
function fuse($left, $right){
	foreach($right as $key=>$value){
		if(!isset($left[$key]))
			$left[$key] = $value;
	}
	
	return $left;
}

//helper function to grab the shit out of the session variable and format it into a message
//this can be done better... like all of it
function makeMessages(){
	$messages = array();
	foreach($_SESSION['created'] as $key => $value){
		$messages[] = "Created " . $value . " " . $key;
	}
	
	
	foreach($_SESSION['updated'] as $key => $value){
		$messages[] = "Updated " . $value . " " . $key;
	}
	
	return $messages;
}