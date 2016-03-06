angular.module('inLeaderBoard',[]).controller('LeaderBoardController',['$scope','$http', function($scope, $http)
{

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
	$scope.GetAllUser = function(){
	
		$http({ method: 'GET',
	        url: 'http://localhost:8080/WebApp/users',
			params: null,
	     	}).success(function(response){
	    	 $scope.GetUsersResult = angular.copy(response);
	     }).error(function (error){
	    	 	alert("hereeee");
	            $scope.status = 'Unable to connect' + error.message;
	     }); 
	}
	
	$scope.goTouserprofile = function(obj)
	{
		alert(obj.user.nickName);
		$scope.showUsrProfile = obj;
		window.location.assign("userProfile.html");
	}
	$scope.CheckSession();
	$scope.GetAllUser();
}]);