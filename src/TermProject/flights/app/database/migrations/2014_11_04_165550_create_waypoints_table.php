<?php

use Illuminate\Database\Schema\Blueprint;
use Illuminate\Database\Migrations\Migration;

class CreateWaypointsTable extends Migration {

	/**
	 * Run the migrations.
	 *
	 * @return void
	 */
	public function up()
	{
		Schema::dropIfExists("WayPoints");
		Schema::create("WayPoints", function($table){
			$table->increments("id");
			$table->string("FlightId")->references("FlightId")->on("Flights")->index();
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
		Schema::drop("WayPoints");
	}

}
