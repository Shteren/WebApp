
angular.module('MainApp',[]).controller('LeaderBoardController',['$scope','$http', function($scope, $http)
{

	
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
		$scope.showProfile = false;
		
	}
	$scope.GetAllUser = function(){
	
		$http({ method: 'GET',
	        url: 'http://localhost:8080/WebApp/users',
			params: null,
	     	}).success(function(response){
	    	 $scope.GetUsersResult = angular.copy(response);
	     }).error(function (error){
	            $scope.status = 'Unable to connect' + error.message;
	     }); 
	}
	
	$scope.goTouserprofile = function(obj)
	{
		$scope.showProfile = true;
		$scope.currentUser = obj;
	}
	$scope.checkDes = function(obj){
		if (obj==null){
			return false;
		}else{
			return true;
		}
		
	}
	$scope.isEmpt= function(obj){
		
		if (obj.length==0){
			return false;
		}else{
			return true;
		}
		
	}
	$scope.showProfile = false;
	$scope.CheckSession();
	$scope.GetAllUser();
}]);