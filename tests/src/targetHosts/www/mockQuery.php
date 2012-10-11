<?php

$headers = getallheaders();
$query_string = urldecode($_SERVER['QUERY_STRING']);
$uri = urldecode($_SERVER['REQUEST_URI']);

if(strlen($query_string)) {
	Header('TEST_QUERY: ' . $query_string);
}

if(strlen($uri)) {
	Header('TEST_URI: ' . $uri);
}

?>
