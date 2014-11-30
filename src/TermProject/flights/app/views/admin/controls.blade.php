<div id="controlBlock" class="row">
	<h1 class="col-sm-12">Admin Controls</h1>
	<div class="col-md-6">
		
		{{ Form::open(array("url"=>"/admin/update/airports")) }}
			<div class="form-group">
				{{ Form::label("updateAirports", "Update airports") }}
				{{ Form::submit("Update", array("name"=>"updateAirports", "class"=>"btn btn-update")) }}
			</div>
		{{ Form::close() }}
		
		{{ Form::open(array("url"=>"/admin/update/airlines")) }}
			<div class="form-group">
				{{ Form::label("updateAirlines", "Update airlines") }}
				{{ Form::submit("Update", array("name"=>"updateAirlines", "class"=>"btn btn-update")) }}
			</div>
		{{ Form::close() }}
		
		<?php
			$states = State::all();
			
			$labels = array();
			foreach($states as $state){
				$labels[$state['StateCode']] = $state['StateCode'];
			}
		?>
		{{ Form::open(array("url"=>"/admin/update/flights")) }}
			<div class="form-group">
				{{ Form::label("updateFlightsByState", "Update flights by state") }}
				{{ Form::select("state", $labels, '', array("class"=>"warning-trigger")) }}
				<p class="form-description warning">Running this function will request flights for <i>all</i> airports in this state.</p>
				{{ Form::submit("Update", array("name"=>"updateFlightsByState", "class"=>"btn btn-update")) }}
			</div>
		{{ Form::close() }}
		
		{{ Form::open(array("url"=>"/admin/update/tracks")) }}
			<div class="form-group">
				{{ Form::label("updateFlightTracks", "Update waypoints for flights recently landed") }}
				{{ Form::submit("Update", array("name"=>"updateFlightTracks", "class"=>"btn btn-update")) }}
			</div>
		{{ Form::close() }}
		
		{{ Form::open(array("url"=>"/admin/update/all")) }}
			<div class="form-group">
				{{ Form::label("updateAll", "God mode") }}
				{{ Form::submit("Update All", array("name"=>"updateAll", "class"=>"btn btn-update")) }}
				<p class="form-description">Running this function will update all flights for today. It will poll all airlines in all states. Give it some time</p>
			</div>
		{{ Form::close() }}
	
	</div>
	
	<div class="col-md-6">
		{{ Form::open(array("url"=>"/admin/delete/Logger")) }}
			<div class="form-group">
				{{ Form::label("deleteLogger", "Delete (all) Logs") }}
				{{ Form::submit("Delete", array("name"=>"updateAll", "class"=>"btn btn-delete")) }}
			</div>
		{{ Form::close() }}
	</div>

</div>