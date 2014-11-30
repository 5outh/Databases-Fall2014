<?php

use Illuminate\Database\Schema\Blueprint;
use Illuminate\Database\Migrations\Migration;

class CreateCitiesTable extends Migration {

	/**
	 * Run the migrations.
	 *
	 * @return void
	 */
	public function up()
	{
		Schema::dropIfExists("Cities");
		Schema::create("Cities", function($table){
			$table->string("CityCode")->primary();
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
	}

}
