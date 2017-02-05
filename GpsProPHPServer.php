<?php
define("MYSQL_SERVER", "localhost");
define("MYSQL_USER", "id725871_investdata");
define("MYSQL_PASSWORD", "");
define("MYSQL_DB", "id725871_investdata");
define("CREATE_DB_IF_NOT_EXISTS", true);
define("CREATE_TABLES_IF_NOT_EXIST", true);
define("LOG_IP", true);
define("LOG_IP_IGNORE", "78.201.68.");
define("DISABLE_DETAILED_LOG_VIEW", true);

    // CREATE DB IF NOT EXISTS
    $db = new mysqli(MYSQL_SERVER, MYSQL_USER, MYSQL_PASSWORD);
    if ($db->connect_errno) {        
        exit;
    }

if (isset($_GET['uploadcoord'])) {
    $coord = $_GET['uploadcoord'];
    echo "received=  [[$coord]]<br/>";
    $array = explode(";", $coord);
    
    foreach($array as $c){
        echo $c . "<br/>";
    }

    $device = "";
    $lat = "";
    $lng = "";
    $alt = "";
    if (count($array)==4){
        $device = $array[0];
        $lat = $array[1];
        $lng = $array[2];
        $alt = $array[3];
    }
       
    $r = mysqli_query($db, "insert into id725871_investdata.gpspro_coordinates (device, lat, lng, alt) values ('" . $device . "', '" . $lat . "', '" . $lng . "', '" . $alt . "')");

    echo "mysqli error = " . mysqli_error($db) . "<br/>";
    
    if (DEBUG) echo 'Coordinates recorded OK.<br/>';
    
    $db->close();
    
    exit;
}

?>
