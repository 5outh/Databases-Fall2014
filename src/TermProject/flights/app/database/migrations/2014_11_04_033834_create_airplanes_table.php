<?php

use Illuminate\Database\Schema\Blueprint;
use Illuminate\Database\Migrations\Migration;

class CreateAirplanesTable extends Migration {

	/**
	 * Run the migrations.
	 *
	 * @return void
	 */
	public function up()
	{
		Schema::dropIfExists("AirPlanes");
		Schema::create("AirPlanes", function($table){
			$table->string("AirPlaneCode")->primary();
			$table->string("AirPlaneName");
		});
	}

	/**
	 * Reverse the migrations.
	 *
	 * @return void
	 */
	public function down()
	{
		Schema::drop("AirPlanes");
	}

}
	