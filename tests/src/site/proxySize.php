<data>
<?php

$headers = apache_request_headers();
$size = 0;

if (isset($headers['SIZE']) || isset($headers['size']))
{
	$size = isset($headers['SIZE']) ? $headers['SIZE'] : (isset($headers['size']) ? isset($headers['size']) : 0) ;
}

$str = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

while ( $size > strlen($str)) {
    print $str;
    $size -= strlen($str);
}

while ( $size > 0 ) {
    print $str[$size-1];
    $size--;
}

?>
</data>