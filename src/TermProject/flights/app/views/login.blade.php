<h1>Login</h1>
@if($errors->any())
	{{ $errors->first() }}
@endif
{{ Form::open(array("url"=>"/login")) }}
	{{ Form::text("username", "Username") }}
	{{ Form::label("password", "Password") }}
	
	{{ Form::password("password") }}
	{{ Form::submit("Submit") }}
{{ Form::close() }}