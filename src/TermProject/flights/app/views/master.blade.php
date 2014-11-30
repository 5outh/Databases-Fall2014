<!DOCTYPE html>
<html>
<head>
<title>Flight Tracker Interface</title>
{{ HTML::script("js/jquery.js") }}
{{ HTML::script("js/flights.js") }}
{{ HTML::script("js/DataTables/media/js/jquery.dataTables.min.js") }}

{{ HTML::script("js/DataTables/media/css/jquery.dataTables.min.css") }}
{{ HTML::style("css/flights.css") }}
{{ HTML::style("css/bootstrap.min.css") }}

</head>
<body>
	@yield("content")
</body>
</html>