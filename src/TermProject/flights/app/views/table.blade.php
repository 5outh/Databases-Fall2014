@if(!isset($data[0]))
	<h1>There are no results for that type</h1>
@else

<div class="data-table-container">
	<table class="data-table">
		<thead>
			<tr>
				@foreach($data[0]->getAttributes() as $key => $value)
					<th>{{ $key }}</th>
				@endforeach
			</tr>
		</thead>
		<tbody>
			<?php $i = 0; ?>
			@foreach($data as $datum)
				<tr class="{{ $i % 2 == 0 ? 'even' : 'false' }}">
					@foreach($datum->getAttributes() as $key=>$value)
						<td>{{ $datum->$key}}</td>
					@endforeach
				</tr>	
				
				<?php $i++; ?>
			@endforeach
		</tbody>
	</table>
</div>
@endif