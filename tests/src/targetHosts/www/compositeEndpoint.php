<?php
// do not forget to update this file on all agents

Header('Content-type: text/xml');
$headers = getallheaders();

if(isset($headers['E3Test-InjectOAPStatusCodeHeader']) && $headers['E3Test-InjectOAPStatusCodeHeader'] == 'true') {
        Header('OAP_StatusCode: 200');
}
?>
<data>
<?php if(isset($headers['X-LogCorrelationID'])):?><correlationId><?= $headers['X-LogCorrelationID']; ?></correlationId><?php endif; ?>
<?php if(isSet($headers['X-SubscriberID'])):?><subscriberId><?= $headers['X-SubscriberID']; ?></subscriberId><?php endif; ?>
</data>
