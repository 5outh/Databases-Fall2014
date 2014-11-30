<?php

use Illuminate\Database\Schema\Blueprint;
use Illuminate\Database\Migrations\Migration;

class Schematwo extends Migration {

	/**
	 * Run the migrations.
	 *
	 * @return void
	 */
	public function up()
	{
		Schema::drop("Cities");
		Schema::drop("States");
		Schema::drop("PilotPay");
		Schema::drop("Results");
		Schema::drop("Taxes");
		Schema::drop("AirLines");
		Schema::drop("Flights");
		Schema::drop("AirPlanes");
		Schema::drop("Airports");
		
		Schema::create("PilotPay", function($table){
			$table->increments("id");
			$table->string('ALCode')->references("ALCode")->on("AirLines");
			$table->string("PilotRank");
			$table->string("AirPlaneCode")->references("AirPlaneCode")->on("AirPlanes");
			$table->string("Pay");
			//$table->unique(array("ALCode", "PilotRank", "AirPlaneCode")); //make this a composite key. each airline will have pay ranks different based on the airplane they are flying
			$table->timestamps();
		});
		
		Schema::create("AirPlanes", function($table){
			$table->increments("id");
			$table->string("AirPlaneCode")->unique()->index();
			$table->string("AirPlaneName");
		});
		
		Schema::create("Flights", function($table){
			$table->increments("id");
			$table->string("FlightId")->unique()->index();
			$table->string("dept")->references("APCode")->on("AirPorts");
			$table->string("dest")->references("APCode")->on("AirPorts");
			$table->string("time_dept");
			$table->string("time_dest");
			//duration?
			$table->string("ALCode")->references("ALCode")->on("AirLines");
			$table->string("AirPlaneCode")->references("AirPlaneCode")->on("AirPlanes");			
		});
		
		Schema::create("AirLines", function($table){
			$table->increments("id");
			$table->string("ALCode")->unique()->index();
			$table->string("AirlineName");
		});
		
		Schema::create("Airports", function($table){
			$table->increments("id");
			$table->string("APCode")->unique()->index();
			$table->decimal("lat", 10, 5);
			$table->decimal("lon", 10, 5);
			$table->string("AirportName");
			$table->string("Street1");
			$table->string("Street2");
			$table->integer("Zip");
			$table->string("CityCode")->references("CityCode")->on("Cities");
			$table->string("StateCode")->references("StateCode")->on("States");
		});
		
		Schema::create("Taxes", function($table){
			$table->increments("id");
			$table->string("StateCode")->references("StateCode")->on("States");
			$table->decimal("BracketStart", 10, 2);
			$table->decimal("BracketEnd", 10, 2);
			$table->decimal("IncomeTax", 10, 5);
		});
		
		Schema::create("Results", function($table){
			$table->increments("id");
			$table->string("FlightId")->references("FlightId")->on("Flights")->unique()->index();
			$table->decimal("CurrentEarnings", 10, 2);
			$table->decimal("ProposedEarnings", 10, 2);
		});
		
		Schema::create("States", function($table){
			$table->increments("id");
			$table->string("StateCode")->unique()->index();
			$table->string("StateName");
		});	
		
		Schema::create("Cities", function($table){
			$table->increments("id");
			$table->string("CityCode")->unique()->index();
			$table->string("StateCode")->references("StateCode")->on("States");
			$table->string("CityName");
			$table->decimal("lat", 10, 5);
			$table->decimal("lon", 10, 5);
		});
	}

	/**
	 * Reverse the migrations.
	 *
	 * @return void
	 */
	public function down()
	{
		Schema::drop("Cities");
		Schema::drop("States");
		Schema::drop("PilotPay");
		Schema::drop("Results");
		Schema::drop("Taxes");
		Schema::drop("AirLines");
		Schema::drop("Flights");
		Schema::drop("AirPlanes");
		Schema::drop("Airports");
	}

}