<?php

use Illuminate\Database\Schema\Blueprint;
use Illuminate\Database\Migrations\Migration;

class CreateFlightsTable extends Migration {

	/**
	 * Run the migrations.
	 *
	 * @return void
	 */
	public function up()
	{
		Schema::dropIfExists("Flights");
		Schema::create("Flights", function($table){
			$table->string("FlightId")->primary();
			$table->string("dept")->references("APCode")->on("AirPorts");
			$table->string("dest")->references("APCode")->on("AirPorts");
			$table->string("time_dept");
			$table->string("time_dest");
			//duration?
			$table->string("ALCode")->references("ALCode")->on("AirLines");
			$table->string("AirPlaneCode")->references("AirPlaneCode")->on("AirPlanes");			
		});
	}

	/**
	 * Reverse the migrations.
	 *
	 * @return void
	 */
	public function down()
	{
		Schema::drop("Flights");
	}

}
