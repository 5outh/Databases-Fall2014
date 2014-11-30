<?php

/*
|--------------------------------------------------------------------------
| Application Routes
|--------------------------------------------------------------------------
|
| Here is where you can register all of the routes for an application.
| It's a breeze. Simply tell Laravel the URIs it should respond to
| and give it the Closure to execute when that URI is requested.
|
*/

Route::get('/', function()
{
	return View::make('login');
});

Route::get("login", function(){
	return View::make("login");
});
Route::post("login", "UserController@login");

Route::filter('checkAdmin', function(){
	//route to login if not auth
	
	if(!(Auth::check())){
		return Redirect::to('login');
	}
	
	//if auth and requested is login, then send them to admin main
	else if(Route::currentRouteName() == 'login'){
		return Redirect::to('admin.home');
	}
	
});
/*********************************************************
*
*	Data Views
*
**********************************************************/
Route::get('data/view/{type}', 'DataController@view');

Route::get("data/test", "DataController@test");

/*********************************************************
*
*	Admin Controls 
*
***********************************************************/
Route::when('admin*', 'checkAdmin');

Route::get("admin", function(){
	return View::make("admin.home");
});

Route::post("admin/create/user", "UserController@create");

Route::post("admin/update/{what}", "DataController@update");

Route::post("admin/delete/{what}", "DataController@delete");

Route::get("test", "FlightTracker@test");

/***
*
*	API
*
***/
Route::get("api/airports/cc/{countrycode}", "FlightTracker@airportsByCountryCode");

Route::get("api/airlines/all", "FlightTracker@allAirlines");

Route::get("api/airport/status/{apcode}/{year}/{month}/{day}/{hour?}/{numHours?}/{maxFlights?}", "FlightTracker@flightStatusByAirport");

Route::get("api/flight/status/{flightId}", "FlightTracker@flightStatus");

Route::get("api/flight/tracks/{flightId}/{maxPositions?}", "FlightTracker@flightTracks");


Route::get("api/geocode/reverse/{lat}/{lon}", "Geolocator@getState");