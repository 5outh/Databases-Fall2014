<?php

use Illuminate\Database\Schema\Blueprint;
use Illuminate\Database\Migrations\Migration;

class CreatePilotPayTable extends Migration {

	/**
	 * Run the migrations.
	 *
	 * @return void
	 */
	public function up()
	{
		Schema::dropIfExists("PilotPay");
		Schema::create("PilotPay", function($table){
			$table->increments("id");
			$table->string('ALCode')->references("ALCode")->on("AirLines");
			$table->string("PilotRank");
			$table->string("AirPlaneCode")->references("AirPlaneCode")->on("AirPlanes");
			$table->string("Pay");
			//$table->unique(array("ALCode", "PilotRank", "AirPlaneCode")); //make this a composite key. each airline will have pay ranks different based on the airplane they are flying
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
		Schema::drop("PilotPay");
	}

}
