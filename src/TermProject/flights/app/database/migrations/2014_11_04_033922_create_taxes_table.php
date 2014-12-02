<?php

use Illuminate\Database\Schema\Blueprint;
use Illuminate\Database\Migrations\Migration;

class CreateTaxesTable extends Migration {

	/**
	 * Run the migrations.
	 *
	 * @return void
	 */
	public function up()
	{
		Schema::dropIfExists("Taxes");
		Schema::create("Taxes", function($table){
			$table->increments("id");
			$table->string("StateCode")->references("StateCode")->on("States");
			$table->decimal("BracketStart", 10, 2);
			$table->decimal("BracketEnd", 10, 2);
			$table->decimal("IncomeTax", 10, 5);
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
		Schema::drop("Taxes");
	}

}