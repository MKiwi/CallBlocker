<?php

 /* commands:
    report [id] [number] 
    download [id]
    check [id] [number]  
 
 */
 header('Content-Type: text/html; charset=utf-8');
 
  if( isset($_GET['command']) && isset($_GET['user']) && isset($_GET['number'])) {
	 $user=strip_tags($_GET["user"]);
	 $number=strip_tags($_GET["number"]);
	 $command=strip_tags($_GET["command"]);
	 $command=htmlspecialchars_decode($command);
	 $user=htmlspecialchars($user);
	 $number=htmlspecialchars($number);
  	 addslashes($user);
  	 addslashes($number);
  	 addslashes($command);
  	 
  	 if (strcmp($command, "report")==0) {
    block($user,$number);   
	}

     if (strcmp($command, "check")==0) {
    check_number($user,$number);
     
  } 	
  } 
  
  
  if(isset($_GET['command']) && isset($_GET['user'])) {
  	$user=strip_tags($_GET["user"]);
  	$command=strip_tags($_GET["command"]);
  	$user=htmlspecialchars($user);
   $command=htmlspecialchars_decode($command);
  	addslashes($user);
  	addslashes($command);
  	
   if(strcmp($command, "download")==0) {  	
  	
  	if(check_user($user)) {
  		
  		
  		download($user,$number);
  	
  	 }  
  	 
  	 } }
  

function spoj () {
// not implemented reason: SECURITY
return false;
}

 function vyber($link) { if (mysql_select_db('ivancik22', $link))
 {mysql_query("SET CHARACTER SET 'utf8'", $link); return true;  }  else return
 false; }

 function check_user($user){
 	
 	      if ($link = spoj()) {  
    
        	if (vyber($link)) { 
			$sql = "SELECT * FROM APP_Users WHERE ID=".'"'.$user.'"'." AND Allowed=1"; 
        	$result = mysql_query($sql, $link);
		   	//echo "sql = $sql <br>";
				
			
			if ($result ) {
			       if (mysql_num_rows($result)==1) return true;
			       else return false;
	            
			} else {
				// dopyt sa NEpodarilo vykonať!
				echo 'ERROR could not check user'; //session_unset(); 
				return false;
			}
		} else {
			// NEpodarilo sa vybrať databázu!
			echo 'ERROR unable to choose database'; //session_unset(); 
			return false;
		}
		mysql_close($link);
        
      
     } else  {echo"ERROR could not connect to server"; } 
     return false;
 } 
 
 
  function check_number($user,$number) { 
  
   $info="";
   $date;
   $date_valid=true;
 
  	      if (check_user($user) && $link = spoj()) {  
    
        	if (vyber($link)) { 
			$sql = "SELECT *, COUNT(Number) AS pocet FROM APP_Numbers WHERE Number=".$number." ORDER BY timestamp DESC"; 
        	$result = mysql_query($sql, $link);
		   //	echo "sql = $sql <br>";			
			
			if ($result) {
				
				$num_rows=mysql_num_rows($result);
				
				 if ($num_rows==0) { echo "ERROR number has not been reported yet"; }

					else {
					
					
					while($row = mysql_fetch_assoc($result)) {
					
						if($date_valid==true) {$date=$row['Timestamp']; $date_valid=false;}	
						if($row['Info']!=1) { $info=$row['Info']; str_replace('&',':',$info); break; }
						      
			       }
			       
				   if($row['pocet']==0) { echo "ERROR number has not been reported yet";} else {
				   
			       echo "OK&".$info."&".$date."&".$row['pocet']."&!"; }
					
					}
			       
			       
			      
	            
			} else {
				// dopyt sa NEpodarilo vykonať!
				echo 'ERROR could not check number'; //session_unset(); 
				return true;
			}
		} else {
			// NEpodarilo sa vybrať databázu!
			echo 'ERROR unable to choose database'; //session_unset(); 
			return true;
		}
		mysql_close($link);
        
      
     } else  {echo"ERROR could not connect to server"; return true; } 
     
 } 
 

 function download($user,$number) { 
 
  	      if ($link = spoj()) {  
    
        	if (vyber($link)) { 
			$sql = "SELECT * FROM APP_toBlock"; 
        	$result = mysql_query($sql, $link);
		   	//echo "sql = $sql <br>";			
			
			if ($result ) {
				
			       
			       while($row = mysql_fetch_assoc($result)) {
						echo $row['Number'];
						echo "&";			       
			       }
			       echo"!";
	            
			} else {
				// dopyt sa NEpodarilo vykonať!
				echo 'ERROR could not check duplicity'; //session_unset(); 
				return true;
			}
		} else {
			// NEpodarilo sa vybrať databázu!
			echo 'ERROR unable to choose database'; //session_unset(); 
			return true;
		}
		mysql_close($link);
        
      
     } else  {echo"ERROR could not connect to server"; return true; } 
     
 } 
 
 
 function add_to_blocklist($number) { 
 
  	      if ($link = spoj()) {  
    
        	if (vyber($link)) { 
			$sql = "INSERT INTO APP_toBlock VALUES (".$number.")" ; 
        	$result = mysql_query($sql, $link);
		   	//echo "sql = $sql <br>";
				
			
			if ($result ) {
			      
	            
			} else {
				// dopyt sa NEpodarilo vykonať!
				echo 'ERROR could not add to blocklist'; //session_unset(); 
				return true;
			}
		} else {
			// NEpodarilo sa vybrať databázu!
			echo 'ERROR unable to choose database'; //session_unset(); 
			return true;
		}
		mysql_close($link);
        
      
     } else  {echo"ERROR could not connect to server"; return true; } 
     
 } 
 
 
 function duplicity_report($user,$number) { 
 
  	      if ($link = spoj()) {  
    
        	if (vyber($link)) { 
			$sql = "SELECT * FROM APP_Numbers WHERE reported_by=".'"'.$user.'"'." AND Number=".$number; 
        	$result = mysql_query($sql, $link);
		   	//echo "sql = $sql <br>";
				
			
			if ($result ) {
			       if (mysql_num_rows($result)>=1) {echo"ERROR duplicit report"; return true;}
			       else {return false; }
	            
			} else {
				// dopyt sa NEpodarilo vykonať!
				echo 'ERROR could not check duplicity'; //session_unset(); 
				return true;
			}
		} else {
			// NEpodarilo sa vybrať databázu!
			echo 'ERROR unable to choose database'; //session_unset(); 
			return true;
		}
		mysql_close($link);
        
      
     } else  {echo"ERROR could not connect to server"; return true; } 
     
 }
  


 function block($user,$number) {
 	
      if(!check_user($user)) return;	
      
      if(duplicity_report($user,$number)) return;
 
      if ($link = spoj()) {  
    
        	if (vyber($link)) { 
			
			if(isset($_GET['info'])) {
			
			$info=$_GET['info'];
			$sql = "INSERT INTO APP_Numbers (Number, Info, reported_by) VALUES (".$number.",'".$info."',".$user.")"; 
			
			}
			
			else {
			
			$sql = "INSERT INTO APP_Numbers (Number, Info, reported_by) VALUES (".$number.",1,".$user.")"; 
			
			}
			
        	$result = mysql_query($sql, $link);
		   	//echo "sql = $sql <br>";
		   	
		   	
				
			
			if ($result ) {
								
			 $sql= "SELECT count(Number) AS Num from APP_Numbers where Number=".$number;
			 
			     $result = mysql_query($sql, $link);			     
			     $row = mysql_fetch_assoc($result);
						
						echo"OK report ";		 			

						if($row['Num']==3){ add_to_blocklist($number);}			       
			       
	            
			} else {
				// dopyt sa NEpodarilo vykonať!
				echo 'ERROR could not add number'; //session_unset(); 
			}
		} else {
			// NEpodarilo sa vybrať databázu!
			echo 'ERROR unable to choose database'; //session_unset(); 
		}
		mysql_close($link);
        
      
     } else  {echo"ERROR could not connect to server"; }  
 
 
 }




?>