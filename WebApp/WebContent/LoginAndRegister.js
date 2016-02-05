/**
 * 
 */

angular.module('inTableApp',[])
	.controller('inTableController',['$scope','$http', function($scope, $http) {
			$scope.register = function(){
				//validate rules for username
				if(null == $scope.RegistrationName )
				{
					alert("Please Fill the Username Field");
					return;
				}
				if($scope.RegistrationName.length > 10 )
				{
					alert("Max characters in username is 10");
					return;
				}
				$http(
					 {
			         method: 'POST',
			         url: 'http://localhost:8080/WebApp/RegisterServlet',
						params: {
							userName: $scope.RegistrationName,
							password: $scope.RegistrationPassword,
							nickName: $scope.RegistrationNickname,
							description: $scope.RegistrationDescription,					
						},
			         //params: { username: $scope.U_Name, password: $scope.U_PWD, action: "login" },
					headers: {'Content-Type': 'application/x-www-form-urlencoded'}
			     })
		     .success(function (result) 
		    	     {
		    	         if (result == true) 
		    	         {
		    	             alert('user is valid');
		    	         }
		    	         else 
		    	         {
		    	             alert('unauthorised access!');
		    	         }
		    	     })
		    	     .error(function (error) 
		    	     {
		    	             $scope.status = 'Unable to connect' + error.message;
		    	     });  
		}
			
			
		$scope.login = function(){
			$http({
				  method: 'GET',
				  url: 'http://localhost:8080/WebApp/LoginServlet',
					params: {
						userName: $scope.name,
						password: $scope.password,			
					}
			}).then(function successCallback(response) {
			    // this callback will be called asynchronously
			    // when the response is available
			  }, function errorCallback(response) {
			    // called asynchronously if an error occurs
			    // or server returns response with an error status.
			  });
		}

}]);

