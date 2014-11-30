<?php

use Illuminate\Auth\UserTrait;
use Illuminate\Auth\UserInterface;
use Illuminate\Auth\Reminders\RemindableTrait;
use Illuminate\Auth\Reminders\RemindableInterface;

class Airport extends Eloquent implements UserInterface, RemindableInterface {

	use UserTrait, RemindableTrait;

	/**
	 * The database table used by the model.
	 *
	 * @var string
	 */
	protected $table = 'Airports';

	/**
	 * The attributes that can be filled
	 *
	 * @var array
	 */
	protected $fillable = array("APCode", "lat", "lon", "AirportName", "Street1", "Street2", "Zip", "CityCode", "StateCode");
}