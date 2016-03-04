/**
 * 
 */

angular.module('inTopicQuestion',[])
	.controller('TopicQuestionController',['$scope','$http', function($scope, $http) {
		$scope.CheckNextButton = function()
		{
			if( $scope.NumOfPages == $scope.prevOrNextPageNumCounter )
	   		 {
		    		 $scope.NextButtonFlag = true; // disabled
	   		 }
		    	 else
	   		 {
		    		 $scope.NextButtonFlag = false; // enabled
	   		 }
		}
		$scope.CheckPreviousButton = function()
		{
	    	
	    	 if( 0 == $scope.prevOrNextPageNumCounter )
    		 {
	    		 $scope.PreviousButtonFlag = true; // disabled
    		 }
	    	 else
    		 {
	    		 $scope.PreviousButtonFlag = false; // enabled
    		 }
		}
		$scope.switchAnswerShowFlag = function()
		{
			$scope.AnswerShowFlag = !$scope.AnswerShowFlag;
		};
		
		$scope.GetQuestionsByTopic = function()
		{
			if(null == $scope.topic)
			{
				alert("Can't find Blank topic");
				return;
			}
			$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/questions',
		    	params:{
		    		topicName:$scope.topic,
		    		currentPage: "0"
		    	}
		     })
		     .success(function(response) 
		     {
		    	 if(null == response || 0 == response.length)
		    	 {
		    		 alert("Not Found");
		    		 return;
		    	 }
		    	 $scope.NumOfPages = response.numOfPages ;
		    	 $scope.CheckNextButton();
		    	 $scope.CheckPreviousButton();
		    	 //$scope.PreviousButtonFlag = true; // disable previous button
		    	 $scope.GetQuestionsResult = angular.copy(response.questions);
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });  
		}
		
		$scope.GetQuestionsAns = function(obj)
		{
			if (obj.showAns == true){
				$scope.falseShow(obj)
			}else
			{
				$http({ method: 'GET',
			        url: 'http://localhost:8080/WebApp/questions/'+obj.questionId +'/answers',
			     })
			     .success(function(response) 
			     {
			    	 obj.showAns = true;
			    	 if(null == response || 0 == response.length)
			    	 {
			    		 alert("Not Found");
			    		 return;
			    	 }
			    	 //$scope.NumOfPages = response.numOfPages ;
			    	 //$scope.CheckNextButton();
			    	 //$scope.CheckPreviousButton();
			    	 //$scope.PreviousButtonFlag = true; // disable previous button
			    	 //$scope.GetAnswerResults = angular.copy(response);
			    	 obj.GetAnswerResults = response.slice(1, response.length);;
			     })
			     .error(function (error) 
			     {
			             $scope.status = 'Unable to connect' + error.message;
			     });  
			}
			
		}
		$scope.falseShow = function(obj)
		{
			obj.showAns = false;
			
		}
		//$scope.HideShowAns = function()
		//{	
			
		//	$scope.showAns = !$scope.showAns;
		//};
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
		           //alert('Resistration was successful');
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
		
		$scope.logOut=function(){
		     
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
		$scope.voteUp = function(obj){
	    	
			$http({ method: 'PUT',
		        url: 'http://localhost:8080/WebApp/questions/'+obj.questionId,
		        data: { questionVote: +1},
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}
		     })
		     .success(function(result) 
		     {
		    	 
		    	 if ((result.Result == "It's your question")||(result.Result == "The user already vote")){
		    		 alert(result.Result); 
		    	 }else{
		    		obj.questionVote++; 
		    	 }
		    	 
		    	 
		     })
		     .error(function (error) 
		     {
		    	 alert("voteUp")
		             $scope.status = 'Unable to connect' + error.message;
		     }); 
	    }
	    $scope.voteDown = function(obj){
	    	
			$http({ method: 'PUT',
		        url: 'http://localhost:8080/WebApp/questions/'+obj.questionId,
		        data: { questionVote: -1},
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}
		     })
		     .success(function(result) 
		     {
		    	 if ((result.Result == "It's your question")||(result.Result == "The user already vote")){
		    		 alert(result.Result); 
		    	 }else{
		    		obj.questionVote--; 
		    	 }
		     })
		     .error(function (error) 
		     {
		    	 alert("voteDown");
		             $scope.status = 'Unable to connect' + error.message;
		     }); 
	    }
	    $scope.timeFormat = function(obj)
	    {
	    	
	    	obj.time = obj.submmitionTime.split(" ");
	    	var date = obj.time[0].split("-");
	    	var temp = date[0];
	    	date[0] = date[2];
	    	date[2] = temp;
	    	obj.time[0] = date[0].concat("/", date[1],"/", date[2], " ", obj.time[1]);
	    	obj.time = obj.time[0];
	    	return obj.time
	    };
		
		// Code start from here
		$scope.CheckSession();
		$scope.showAns = false;
	    $scope.NextButtonFlag = true;
	    $scope.PreviousButtonFlag = true;
		$scope.AnswerShowFlag = false;
		$scope.prevOrNextPageNumCounter = 0;
		$scope.GetQuestionsResult = [];
		
}]);