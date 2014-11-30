<?php

class UserController extends BaseController {
	
	public function create(){
		$username = Input::get("username");
		
		$password = Input::get("password");
		
		$password_conf = Input::get("password_confirmation");
		
		$validator = Validator::make(
			array(
				'username' => $username,
				'password' => $password,
				'password_confirmation' => $password_conf
			),
			array(
				'username' => 'required',
				'password' => 'required|min:8|confirmed'
			)
		);
		
		if($validator->fails()){
			return Redirect::back()->with("messages", $validator->messages);
			Logger::make("User", "Failed to create new user");
		}
		
		$user_fields = array(
			"username" => $username,
			"password" => Hash::make($password),
		);
		
		User::create($user_fields);
		
		Logger::make("User", "Created new user: " . $username);
		
		return Redirect::back()->with("messages", array("Successfully created user " . $username));
	}
	
	public function login(){
		$username = Input::get("username");
		$password = Input::get("password");
		
		if(Auth::attempt(array("username"=>$username, "password"=>$password))){
			Logger::make("User", "User " . $username . " successfully logged in");
			return Redirect::to("admin");
			
		}
		else{
			Logger::make("User", "Failed attempt to login in. Username: " . $username);
			return Redirect::back()->withErrors([ "Incorrect password or username combo"]);
		}
	}
}