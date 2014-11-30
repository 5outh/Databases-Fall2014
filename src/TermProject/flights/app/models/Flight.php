<?php

use Illuminate\Auth\UserTrait;
use Illuminate\Auth\UserInterface;
use Illuminate\Auth\Reminders\RemindableTrait;
use Illuminate\Auth\Reminders\RemindableInterface;

class Flight extends Eloquent implements UserInterface, RemindableInterface {

	use UserTrait, RemindableTrait;

	/**
	 * The database table used by the model.
	 *
	 * @var string
	 */
	protected $table = 'Flights';

	/**
	 * The attributes that can be filled
	 *
	 * @var array
	 */
	protected $fillable = array("FlightId", "FlightNumber", "dept", "dest", "time_dept", "time_dest", "ALCode", "AirPlaneCode", "Status");
	
	protected $statusNames = array(
		"A" 	=> 	"Active",
		"C" 	=> 	"Canceled",
		"D" 	=> 	"Diverted",
		"DN"	=> 	"Data source needed",
		"L" 	=> 	"Landed", 
		"NO"	=> 	"Not Operational",
		"R"	=>	"Redirected",
		"S"	=>	"Scheduled",
		"U"	=>	"Unknown",
	);
	
	//flighttracks gives us a statuscode that is 1-2 chars long
	//use this function to translate that to a human readable name
	public function getStatusName($statusCode){
		return isset($this->statusNames[$statusCode]) ? $this->statusNames[$statusCode] : "status dne";
	}
}