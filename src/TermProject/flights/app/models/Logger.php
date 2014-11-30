<?php

use Illuminate\Auth\UserTrait;
use Illuminate\Auth\UserInterface;
use Illuminate\Auth\Reminders\RemindableTrait;
use Illuminate\Auth\Reminders\RemindableInterface;

class Logger extends Eloquent implements UserInterface, RemindableInterface {

	use UserTrait, RemindableTrait;

	/**
	 * The database table used by the model.
	 *
	 * @var string
	 */
	protected $table = 'Logs';
	
	protected $fillable = array("type", "message");
	
	public static $debug = "Debug";
	
	public static $normal = "Normal";
	
	public static $data = "Data Operation";
	
	public static $user = "User Operation";
	
	public static function make($type, $message){
		Logger::create(array("type"=>$type, "message"=>$message));
	}
	
	/** truncate the table **/
	public static function truncate(){
		$total = 0;
		$logs = Logger::all();
		foreach($logs as $log){
			$log->delete();
			$total++;
		}
		
		Logger::make("Normal", "Deleted {$total} logs");
	}

}
