<?php
// do not forget to update this file on all agents

Header('Content-type: text/xml');
?>
<response>
<parameters>
<?php foreach($_GET as $key => $value):?>
<parameter type="get" key="<?= $key; ?>"><?= $value; ?></parameter>
<?php endforeach;?>
<?php foreach($_POST as $key => $value):?>
<parameter type="post" key="<?= $key; ?>"><?= $value; ?></parameter>
<?php endforeach;?>
<?php
$headers = apache_request_headers();
foreach($headers as $key => $value):?>
<parameter type="header" key="<?= $key; ?>"><?= $value; ?></parameter>
<?php endforeach;?>
</parameters>
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
</response>
