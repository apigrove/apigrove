<?php 
    
    $headers = apache_request_headers();
    $wait = 0;
    
    if (isset($headers['WAIT']) || isset($headers['wait']))
    {
	$wait = isset($headers['WAIT']) ? $headers['WAIT'] : (isset($headers['wait']) ? isset($headers['wait']) : 0) ;
    }
    
    usleep($wait);
?><data>
   <status>SUCCESS</status>
</data>