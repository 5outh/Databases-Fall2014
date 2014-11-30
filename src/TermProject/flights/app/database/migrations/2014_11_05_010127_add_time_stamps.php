<?php

use Illuminate\Database\Schema\Blueprint;
use Illuminate\Database\Migrations\Migration;

class AddTimeStamps extends Migration {

	/**
	 * Run the migrations.
	 *
	 * @return void
	 */
	public function up()
	{
		$tables = array("AirLines", "AirPlanes", "Airports", "Cities", "Flights", "PilotPay", "Results", "States", "Taxes", "WayPoints");
		
		foreach($tables as $table){
			Schema::table($table, function($t){
				$t->timestamps();
			});
		}
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
