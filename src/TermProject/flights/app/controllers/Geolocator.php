<?php

//this class provides helper functions to georeferencing stuff
//like placing lat lon inside a state
//or getting information on a city
class Geolocator extends BaseController {
	public $city_info_base = "http://api.sba.gov/geodata/all_links_for_city_of";
	
	private $google_key = "AIzaSyARH8-hO-bdO_8b4wnS-jCGUzFiKmJ-1d0";
	
	public $google_base = "https://maps.googleapis.com/maps/api/geocode/json";
	
	public function cityInfo($city, $stateCode){
		$url = $this->city_info_base . "/{$city}/{$stateCode}.json";
		
		$ch = curl_init();
		
		curl_setopt ($ch, CURLOPT_URL, $url);
		curl_setopt ($ch, CURLOPT_HEADER, 0);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
		
		$result = curl_exec ($ch);
		curl_close($ch);
		$decoded = (array) json_decode($result);

		if(isset($deocded[0])){
			$c = $decoded[0];	
			
			$ret = array();
			$ret['CityName'] = $c->name;
			$ret['lat'] = $c->primary_latitude;
			$ret['lon'] = $c->primary_longitude;
			$ret['StateCode'] = $c->state_abbreviation;
			
			return $ret;
		}
		else
			return false;
			
	}
	
	public function getState($lat, $lon){
		//Logger::make("Data", "Requesting state for (lat, lon) ... ( " . $lat . " , " . $lon . " )");
		$url = $this->google_base . "?latlng={$lat},{$lon}"
					  ."&result_type=administrative_area_level_1"
					  ."&key={$this->google_key}";
		
		$ch = curl_init();
		
		curl_setopt ($ch, CURLOPT_URL, $url);
		curl_setopt ($ch, CURLOPT_HEADER, 0);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
		
		$result = curl_exec ($ch);
		curl_close($ch);
		
		$decoded = (array) json_decode($result);
				
		//check to see if any results exist
		if($decoded["status"] == "ZERO_RESULTS"){
			Logger::make("Error", "( " . $lat . " , " . $lon . " ) could not be found (ZERO_RESULTS)");
			return NULL;
		}
		
		//if they do we have to extract the state code
		else{
			$targets = $decoded['results'][0]->address_components; //targets is the best match. address_components is any array of objects. we need to find the right one
			
			//parse these objects and check in the "types" attribute
			//we are looking for administrative_area_level_1 in the types array (array of strings)
			//if the object has it, then that is the one we want, return the object property : "short_name" (ex: GA)
			
			//to speed stuff up, it looks like this object is normally at index 5, so check there first
			$potential = $targets[5];
			if(in_array("administrative_area_level_1", $potential->types))
				return $potential->short_name;
			
			//else we have to iterate through them
			foreach($targets as $target){
				if(in_array("administrative_area_level_1", $target->types))
					return $target->short_name;
			}
		}
		return "";
	}
	
}