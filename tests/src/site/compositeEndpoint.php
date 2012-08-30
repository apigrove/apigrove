<?php
// do not forget to update this file on all agents

Header('Content-type: text/xml');


$headers = getallheaders();

$correlationId = @$headers['X-LogCorrelationID'];
$subscriberId = @$headers['X-SubscriberID'];
$injectStatusCode = @$headers['E3Test-InjectOAPStatusCodeHeader'];

if($injectStatusCode == 'true') {
        Header('OAP_StatusCode: 200');
}
?>
<data>
<?php if($correlationId):?><correlationId><?= $correlationId; ?></correlationId><?php endif; ?>
<?php if($subscriberId):?><subscriberId><?= $subscriberId; ?></subscriberId><?php endif; ?>
</data>