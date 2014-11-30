<?php
/*********************
*
*Add some stuff to waypoints, flights, airplanes to take better advantage of what flighttracks sends us
*
*
**********************/
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Database\Migrations\Migration;

class AddColumnsToMoreResembleFlighttracks extends Migration {

	/**
	 * Run the migrations.
	 *
	 * @return void
	 */
	public function up()
	{
		Schema::table("Flights", function($table){
			$table->integer("FlightNumber");
			$table->char("Status", 2);
		});
		
		Schema::table("WayPoints", function($table){
			$table->integer("Speed");
			$table->integer("Altitude");
			$table->string("Date");
		});
	}

	/**
	 * Reverse the migrations.
	 *
	 * @return void
	 */
	public function down()
	{
		//
	}

}
