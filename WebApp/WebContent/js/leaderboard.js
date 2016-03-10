
angular.module('MainApp',[]).controller('LeaderBoardController',['$scope','$http', function($scope, $http)
{

	//function receives answer or question and returns its submission time according to the required tamplate    
	$scope.timeFormat = function(obj)
    {
    	if (obj == null) {
    		return;
    	}
    	obj.time = obj.submmitionTime.split(" ");
    	var date = obj.time[0].split("-");
    	var temp = date[0];
    	date[0] = date[2];
    	date[2] = temp;
    	obj.time[0] = date[0].concat("/", date[1],"/", date[2], " ", obj.time[1]);
    	obj.time = obj.time[0];
    	return obj.time
    };
	
    
    $scope.CheckSession = function()
    //check if there is a user connected, if not go to login and register page 
	{
		$http({ method: 'GET',
	        url: 'http://localhost:8080/WebApp/session',
			params: null,
	     })
	     .success(function (response) 
	     {
	         if (response.Result == false) 
	         {
	        	window.location.assign("index.html");
	         }
	         else
        	 {
	        	 $scope.NickName = response.Nickname;
	        	 return true;
        	 }
	        
	     })
	     .error(function (error) 
	     {
	             $scope.status = 'Unable to connect' + error.message;
	     });  
		return false;
	}
	$scope.bakToLaderBoard = function(){
		//back button - on click show leader board again 
		$scope.showProfile = false;
	}
	
	$scope.logOut=function(){
	     //disconnect and go to registration and login page
		$http({ method: 'POST',
	        url: 'http://localhost:8080/WebApp/logout',
	        headers: {'Content-Type': 'application/json'}
	     })
	     .success(function(response) 
	     {
	    	 window.location.assign("index.html");
	     })
	     .error(function (error) 
	     {
	             $scope.status = 'Unable to connect' + error.message;
	     }); 
		
	}
	$scope.GetAllUser = function(){
		//function gets 20 top users
		$http({ method: 'GET',
	        url: 'http://localhost:8080/WebApp/users',
			params: null,
	     	}).success(function(response){
	     		
	    	 $scope.GetUsersResult = angular.copy(response);
	    	 
	     }).error(function (error){
	    	 
	            $scope.status = 'Unable to connect' + error.message;
	     }); 
	}
	
	$scope.goTouserprofile = function(user)
	{
		//set show profile page to true and set user to show to the user we clicked on
		$scope.showProfile = true;
		$scope.currentUser = user;
	}
	$scope.checkDes = function(description){
		//check if description exist, if not ,don't show the label it is surrounded by 
		if (description==null){
			return false;
		}else{
			return true;
		}
		
	}
	$scope.isEmptTopics= function(topicsList){
		//check if topic list exist, if not ,don't show the label it is surrounded by
		if (topicsList.length==0){
			return false;
		}else{
			return true;
		}
		
	}
	
	//page initiation
	$scope.showProfile = false;
	$scope.CheckSession();
	$scope.GetAllUser();
}]);