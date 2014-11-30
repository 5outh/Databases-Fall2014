@extends("master")
@section("content")
<section id="adminHome" class="page-section container-fluid">
	<div class="page-description row">
		<h1 class="col-sm-12">Admin Home</h1>
	</div>
	
	@if(Session::has('messages'))
		<div class="messages row ">
			<ul class="message-list">
			@foreach(Session::get("messages") as $message)
				<li class="message">{{ $message }}</li>
			@endforeach
			</ul>
		</div>
	@endif
	
	<div class="row">
		<div class="col-md-4">
			<h1>Create new user:</h1>
			{{ View::make("user.newform") }}
		</div>
		
		<div class="col-md-8">
			<div id="adminControls">
				{{ View::make("admin.controls") }}
			</div>
		</div>
	</div>
	
	<div id="data" class="row">
		<div class="control-bar row">
			<span class="description col-sm-4">What would you like to view?</span>
			<a class="getContent trigger" href="/data/view/Airport">Airports</a>
			<a class="getContent trigger" href="/data/view/Airline">Airlines</a>
			<a class="getContent trigger" href="/data/view/City">Cities</a>
			<a class="getContent trigger" href="/data/view/State">States</a>
			<a class="getContent trigger" href="/data/view/Flight">Flights</a>
			<a class="getContent trigger" href="/data/view/Result">Results</a>
			<a class="getContent trigger" href="/data/view/Tax">Taxes</a>
			<a class="getContent trigger" href="/data/view/Pay">Pay</a>
			<a class="getContent trigger" href="/data/view/AirPlane">Airplanes</a>
			<a class="getContent trigger" href="/data/view/Logger">Logs</a>
		</div>
		<div id="data-content" class="col-md-12">
		
		</div>
	</div>
</section>
@stop