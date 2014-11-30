<?php

use Illuminate\Database\Schema\Blueprint;
use Illuminate\Database\Migrations\Migration;

class AddWaypoints extends Migration {

	public function up()
	{
		Schema::dropIfExists("WayPoints");
		Schema::create("WayPoints", function($table){
			$table->increments("id");
			$table->string("FlightId")->references("FlightId")->on("Flights")->index();
			$table->decimal("lat", 10, 5);
			$table->decimal("lon", 10, 5);
			$table->string("StateCode")->references("StateCode")->on("States");
			$table->timestamps();
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
