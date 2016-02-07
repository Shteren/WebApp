/**
 * 
 */


angular.module('inTableApp',[])
	.controller('mainPageController',['$scope','$http', function($scope, $http) {
		
		$scope.addQuestion=function(){
		     if(null == $scope.questionTxt)
				{
					alert("Please fill in your question");
					return;
				}
		     if($scope.questionTxt.length > 300)
				{
					alert("Your question is too long");
					return;
				}
		     
			$http({ method: 'POST',
		        url: 'http://localhost:8080/WebApp/Questions',
				params: {
					questionTxt: $scope.questionTxt,
					questionTopics: $scope.topics,
				},
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}
		     })
		     .success(function (result) 
		     {
		         if (result.Result == true) 
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
		
		
	}]);