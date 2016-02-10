angular.module('inTableApp',[])
	.controller('mainPageController',['$scope','$http', function($scope, $http) {
		

		$scope.GetQuestions = function()
		{
			$http({ method: 'POST',
		        url: 'http://localhost:8080/WebApp/GetQuestionsServlet',
				params: null,
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}
		     })
		     .success(function(response) 
		     {
		        for( i =0 ; i < response.length ; i++)
	        	{
		        	$scope.GetQuestionsResult.push(response[i]);
		        	//alert(response[i]);
	        	}
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });  
		}
		
		$scope.CheckSession = function()
		{
			$http({ method: 'POST',
		        url: 'http://localhost:8080/WebApp/GetSessionStatus',
				params: null,
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}
		     })
		     .success(function (result) 
		     {
		         if (result.Result == false) 
		         {
		           //alert('Resistration was successful');
		        	window.location.assign("index.html");
		         }
		        
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });  
		}
		
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
		        url: 'http://localhost:8080/WebApp/QuestionsServlet',
				params: {
					questionTxt: $scope.questionTxt,
					questionTopics: $scope.topics,
				},
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}
		     })
		     .success(function(result) 
		     {
		        alert("Done");
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     }); 
			
		}
		
		// code start from here when page is loading
		$scope.CheckSession();
		
		$scope.GetQuestionsResult =[];
		$scope.GetQuestions();
		
		
		
		
	}]);