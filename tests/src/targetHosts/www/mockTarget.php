<?php
// do not forget to update this file on all agents

define('DB_FILES_PATH', 'mockdb');


if(!(file_exists(DB_FILES_PATH) && is_dir(DB_FILES_PATH))) {
	unlink(DB_FILES_PATH);
	if(!mkdir(DB_FILES_PATH)) {
		echo "Unable to create DB directory";
		exit;
	}
}

if(strstr($_SERVER["REQUEST_URI"], 'ping_service')) {
	$title = 'SCF Ping Service';
	$mode = 'scf_ping';
} else {
	$title = 'Target';
	$mode = 'target';
}

if(isset($_REQUEST['uniqueId'])) {
        $uniqueId = gethostbyname($_REQUEST['uniqueId']);
} else {
	$uniqueId = $_SERVER["REMOTE_ADDR"];
}

function getDBPathForId($mode, $id) {
	return DB_FILES_PATH.'/'.$mode.'_'.$id;
}

function setMockStatusCode($mode, $id, $statusCode) {
        $fp = fopen(getDBPathForId($mode, $id), 'w+');
        fputs($fp, $statusCode);
        fclose($fp);
}

function cleanDBForId($id) {
	unlink(getDBPathForId('scf_ping', $id));
	unlink(getDBPathForId('target', $id));
}

function getMockStatusCode($mode, $id) {
	if(file_exists(getDBPathForId($mode, $id))) {
		$fp = fopen(getDBPathForId($mode, $id), 'r+');
		$status = trim(fread($fp, 1024));
		fclose($fp);
	} else {
		$status = '000';
	}
        return $status;
}

function logMessage($text) {
	error_log($text, 0);
}

function statusHeaderStringFromStatusCode($statusCode) {
        switch($statusCode) {
                case '200':
                        $status = 'HTTP/1.1 200 OK';
                        break;
                case '202':
                        $status = 'HTTP/1.1 202 Accepted';
                        break;
		case '204':
                        $status = 'HTTP/1.1 204 No Content';
                        break;
                case '304':
                        $status = 'HTTP/1.1 304 Not Modified';
                        break;
                case '503':
                        $status = 'HTTP/1.1 503 Service Unavailable';
                        break;
                case '500':
                        $status = 'HTTP/1.1 500 Internal Server Error';
                        break;
                default:
                        $status = 'HTTP/1.1 000 Unknown';
                        break;
        }
        return $status;
}

if(isset($_GET['action'])) {
        $action = $_GET['action'];
}
else {
        $action = ''; //default
}


switch($action) {
	case 'setStatus':
		if(isset($_GET['statusCode'])) {
			logMessage("set mock status code= " . $_GET['statusCode'] . " with unique id= " . $uniqueId);
			setMockStatusCode($mode, $uniqueId, $_GET['statusCode']);
			$statusHeaderString = statusHeaderStringFromStatusCode('202');
		}
		break;
	case 'cleanDB':
		logMessage("clean db" . " with unique id= " . $uniqueId);
		cleanDBForId($uniqueId);
		$statusHeaderString = statusHeaderStringFromStatusCode('204');
		break;
	case 'log':
		logMessage($_GET['text']);
		$statusHeaderString = statusHeaderStringFromStatusCode('200');
		break;
			
	default:
		$statusCode = getMockStatusCode($mode, $uniqueId);
		logMessage("get statusCode= " . $statusCode  . " with unique id= " . $uniqueId);
		$statusHeaderString = statusHeaderStringFromStatusCode($statusCode);
		break;
}


Header($statusHeaderString);
?>
<html>
<head>
<title><?=  $statusHeaderString; ?></title>
</head>
<body>
<h3><?= $title; ?> mock</h3>
[IP address:<ipAddress><?php echo gethostbyname(gethostname()); ?></ipAddress>]
<hr />
<center>
<?php if($mode == 'scf_ping') { ?>
Current status for id <?= $uniqueId; ?>: <?=  $statusHeaderString; ?><br />
 (<a href="?uniqueId=<?= $uniqueId; ?>">refresh</a>) / <a href="?action=setStatus&statusCode=200&uniqueId=<?= $uniqueId; ?>">available</a> - <a href="?action=setStatus&statusCode=503&uniqueId=<?= $uniqueId; ?>">overloaded</a> - <a href="?action=setStatus&statusCode=500&uniqueId=<?= $uniqueId; ?>">unavailable</a>
<?php } else { ?>
Current status: <?=  $statusHeaderString; ?><br />
<?php } ?>
</center>
</body>
</html>
