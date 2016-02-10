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
		         else
	        	 {
		        	 $scope.NickName = result.Nickname;
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
		
		$scope.logOut=function(){
			
		     
			$http({ method: 'POST',
		        url: 'http://localhost:8080/WebApp/logOutServlet',
				params:null,
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}
		     })
		     .success(function(result) 
		     {
		    	 window.location.assign("index.html");
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     }); 
			
		}
		
		$scope.myFunc = function() 
		{
			if( ($scope.topics.length) > 50)
			{
				$scope.HideShowTagCharacterError = true;
				//$scope.topics = $scope.topics.substring(0, 50 );
				return;
			
			}
			$scope.HideShowTagCharacterError = false;
	        if ($scope.topics[ $scope.topics.length -1 ] == ",")
	        {
	        	$scope.tag.push( $scope.topics.substring(0, $scope.topics.length-1 ) );
	        	$scope.topics = "";
	        }
	    };
		
		
		// code start from here when page is loading
	    $scope.HideShowTagCharacterError = false;
	    $scope.tag = [];
		$scope.CheckSession();
		
		$scope.GetQuestionsResult =[];
		$scope.GetQuestions();
		
		
		
		
	}]);