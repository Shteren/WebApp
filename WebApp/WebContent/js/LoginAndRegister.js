/**
 * 
 */	

	angular.module('MainApp',[]).controller('inTableController',['$scope','$http', function($scope, $http) {
		
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
			if((null == $scope.RegistrationName )||(0 == $scope.RegistrationName.length ))
			{
				
				$scope.userNameErrorFlag = true;
				$scope.userNameError="Please Fill the Username Field";
				return;
			}
			if($scope.RegistrationName.length > 10 )
			{
				return;
			}
			if((null == $scope.RegistrationPassword )||(0 == $scope.RegistrationPassword.length ))
			{
				$scope.paawordErrorFlag = true;
				$scope.passwordError="Please Fill the Password";
				return;
			}
			if($scope.RegistrationPassword.length > 8 )
			{
				return;
			}
			if((null == $scope.RegistrationNickname )||(0 == $scope.RegistrationNickname.length ))
			{
				$scope.nickNameErrorFlag = true;
				$scope.nickNameError="Please Fill in your nickname";
				return;
			}
			if($scope.RegistrationNickname.length > 20 )
			{
				return;
			}
			if(null != $scope.RegistrationDescription )
			{
				if($scope.RegistrationDescription.length > 50 )
				{
					return;
				}
			}
			
			
			$http(
				 {
		         method: 'POST',
		         url: 'http://localhost:8080/WebApp/users',
					data: {
						userName: $scope.RegistrationName,
						password: $scope.RegistrationPassword,
						nickName: $scope.RegistrationNickname,
						description: $scope.RegistrationDescription,
						photoUrl: $scope.RegistrationPhoto,
					},
		         //params: { username: $scope.U_Name, password: $scope.U_PWD, action: "login" },
				headers: {'Content-Type': 'application/json'}
		     })
		     .success(function (result) 
		     {
		    	 //alert(result);
		         if (result.Result == "true") 
		         {
		        	 window.location.assign("MainPage.html")
		         }else{
		           alert(result.Result);
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
					//alert(response.Result)
	   	         if (response.Result == true) 
	   	         {
	   	           //alert('Resistration was successful');
	   	        	window.location.assign("MainPage.html");
	   	         }
	   	         else 
	   	         {
	   	        	 alert('Username or Password are incorrect');
	   	         }
				
			 });
		}
	
		
		$scope.switchLoginRegister = function()
		{
			$scope.LoginShowFlag = !$scope.LoginShowFlag;
		};
		
		$scope.checkRegistrationName = function(){
		//function is checking registration name requirements and sets flag and error string accordingly  
			if ($scope.RegistrationName.length > 10){
				$scope.userNameError="Max characters in username is 10";
				$scope.userNameErrorFlag = true;
			}else if ($scope.RegistrationName.length>0){
				$scope.userNameErrorFlag = false;
			}
		};
		$scope.checkRegistrationPassword = function(){
			//function is checking password requirements and sets flag and error string accordingly  
				if ($scope.RegistrationPassword.length > 8){
					$scope.passwordError="Max characters in password is 8";
					$scope.paawordErrorFlag = true;
				}else if ($scope.RegistrationPassword.length>0){
					$scope.paawordErrorFlag = false;
				}
		};
		$scope.checkRegistrationNickname = function(){
			//function is checking registration nickname requirements and sets flag and error string accordingly  
				if ($scope.RegistrationNickname.length > 20){
					$scope.nickNameError="Max characters in nickname is 20";
					$scope.nickNameErrorFlag = true;
				}else if ($scope.RegistrationNickname.length>0){
					$scope.nickNameErrorFlag = false;
				}
		};
		$scope.checkRegistrationDesc = function(){
			//function is checking registration description requirements and sets flag and error string accordingly  
				if ($scope.RegistrationDescription.length > 50){
					$scope.descriptionError="Max characters in desription is 50";
					$scope.descriptionErrorFlag = true;
				}else {
					$scope.descriptionErrorFlag = false;
				}
		};
		//////// Entering here be default-when page loading ////////////	
		$scope.userNameErrorFlag = false; //show or not error
		$scope.userNameError=""; //error init
		$scope.paawordErrorFlag = false; //show or not error
		$scope.passwordError=""; //error init
		$scope.nickNameErrorFlag = false; //show or not error
		$scope.nickNameError=""; //error init
		$scope.descriptionErrorFlag = false; //show or not error
		$scope.descriptionError=""; //error init
		$scope.LoginShowFlag=true;
		$scope.CheckSession();//check if someone is already logged in
		

}]);

