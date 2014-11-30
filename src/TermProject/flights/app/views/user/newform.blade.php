{{ Form::open(array("url"=>"/admin/create/user")) }}
	<div class="form-group">
		{{ Form::label("username", "User Name") }}
		{{ Form::text("username", "", array("class" => "form-control")) }}
	</div>
	
	<div class="form-group">
		{{ Form::label("password", "Password") }}
		{{ Form::password("password", array("class" => "form-control")) }}
	</div>
	
	<div class="form-group">
		{{ Form::label("password_confirmation", "Password Confirmation") }}
		{{ Form::password("password_confirmation", array("class"=>"form-control")) }}
	</div>
	
	<div class="form-group">
		{{ Form::submit("Submit", array("class"=>"btn btn-block btn-submit", "name"=>"submit")) }}
	</div>
{{ Form::close() }}