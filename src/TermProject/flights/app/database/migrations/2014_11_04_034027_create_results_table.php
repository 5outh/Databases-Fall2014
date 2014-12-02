<?php

use Illuminate\Database\Schema\Blueprint;
use Illuminate\Database\Migrations\Migration;

class CreateResultsTable extends Migration {

	/**
	 * Run the migrations.
	 *
	 * @return void
	 */
	public function up()
	{
		Schema::dropIfExists("Results");
		Schema::create("Results", function($table){
			$table->string("FlightId")->references("FlightId")->on("Flights")->unique();
			$table->decimal("CurrentEarnings", 10, 2);
			$table->decimal("ProposedEarnings", 10, 2);
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
		Schema::drop("Results");
	}

}