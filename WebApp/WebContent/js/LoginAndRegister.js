/**
 * 
 */

angular.module('inTableApp',[])
	.controller('inTableController',['$scope','$http', function($scope, $http) {
		
		$scope.CheckSession = function()
		{
			$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/session'
		     })
		     .success(function (response) 
		     {
		         if (response.Result == true) 
		         {
		           //alert('Resistration was successful');
		        	window.location.assign("MainPage.html");
		         }
		        
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });  
		}
		
		$scope.register = function()
		{
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
			if(null == $scope.RegistrationPassword )
			{
				alert("Please Fill the Password Field");
				return;
			}
			if($scope.RegistrationPassword.length > 8 )
			{
				alert("Max characters in Password is 8");
				return;
			}
			if(null == $scope.RegistrationNickname )
			{
				alert("Please Fill the Password Field");
				return;
			}
			if($scope.RegistrationNickname.length > 20 )
			{
				alert("Max characters in Nickname is 20");
				return;
			}
		
			if($scope.RegistrationDescription.length > 50 )
			{
				alert("Description must be not longer then 50 characters");
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
		    	 //alert(result);
		         if (result.Result == true) 
		         {
		           //alert('Resistration was successful');
		        	window.location.assign("MainPage.html")
		         }
		         else 
		         {
		        	 alert('Registration Failed');
		         }
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });  
		}
			
		// login function handler	
		$scope.login = function()
		{
			$http({
				  method: 'GET',
				  url: 'http://localhost:8080/WebApp/login',
				  	params: {
						userName: $scope.name			
					},
					headers: {'password': $scope.password}
					
			}).success( function(response) {
			
	   	         if (response.Result == true) 
	   	         {
	   	           //alert('Resistration was successful');
	   	        	window.location.assign("MainPage.html")
	   	         }
	   	         else 
	   	         {
	   	        	 alert('Login Failed');
	   	         }
				
			 });
		}
		
		
		$scope.switchLoginRegister = function()
		{
			$scope.LoginShowFlag = !$scope.LoginShowFlag;
		};
		
		
		//////// Entering here be default-when page loading ////////////	
		$scope.LoginShowFlag = true;
		
		$scope.CheckSession();
		

}]);

