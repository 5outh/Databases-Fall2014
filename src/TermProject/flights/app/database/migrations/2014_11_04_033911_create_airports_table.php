<?php

use Illuminate\Database\Schema\Blueprint;
use Illuminate\Database\Migrations\Migration;

class CreateAirportsTable extends Migration {

	/**
	 * Run the migrations.
	 *
	 * @return void
	 */
	public function up()
	{
		Schema::dropIfExists("Airports");
		Schema::create("Airports", function($table){
			$table->string("APCode")->primary();
			$table->decimal("lat", 10, 5);
			$table->decimal("lon", 10, 5);
			$table->string("AirportName");
			$table->string("Street1");
			$table->string("Street2");
			$table->integer("Zip");
			$table->string("CityCode")->references("CityCode")->on("Cities");
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
		Schema::drop("Airports");
	}

}
