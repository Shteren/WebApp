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
		
		$scope.showList = function()
		{
			$scope.showHideTopicList = true;
		}
		
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
		    		currentPage: $scope.topicCurrentPage
		    	}
		     })
		     .success(function(response) 
		     {
		    	 $scope.currenTopic = $scope.topic;
		    	 if(null == response || 0 == response.length)
		    	 {
		    		 alert("Not Found");
		    		 return;
		    	 }
		    	 $scope.showHideTopicList = false;
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
		
		$scope.GetQuestionsByTopic = function(obj)
		{
			$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/questions',
		    	params:{
		    		topicName: obj,
		    		currentPage: $scope.topicCurrentPage
		    	}
		     })
		     .success(function(response) 
		     {
		    	 if(null == response || 0 == response.length)
		    	 {
		    		 alert("Not Found");
		    		 return;
		    	 }
		    	 $scope.currenTopic = obj;
		    	 $scope.showHideTopicList = false;
		    	 $scope.NumOfPages = response.numOfPages ;
		    	 $scope.CheckNextButton();
		    	 $scope.CheckPreviousButton();
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
			    	 obj.GetAnswerResults = response.slice(1, response.length);
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
	    $scope.displayListTopics = function(){
	    	$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/topics',
					params: {currentPage:$scope.topicCurrentPage
					},
		     })
		     .success(function (response) 
		     {;
		    	 $scope.NumOfTopicPages = response.numOfPages ;
		    	 alert($scope.NumOfTopicPages);
		    	 $scope.CheckTopicNextButton();
		    	 $scope.CheckTopicPreviousButton();
		    	 $scope.GetTopicsResult = angular.copy(response.topics);
		        
		     })
		     .error(function (error) 
		     {
		    	 alert("here");
		             $scope.status = 'Unable to connect' + error.message;
		     });  
			return false;
	    	
	    }
	    $scope.CheckTopicNextButton = function()
		{

			if( $scope.NumOfTopicPages == $scope.prevOrNextTopicPageNumCounter)
	   		 {
		    		 $scope.NextTopicButtonFlag = true; // disabled
	   		 }
		    	 else
	   		 {
		    		 $scope.NextTopicButtonFlag = false; // enabled
	   		 }
		}
		$scope.CheckTopicPreviousButton = function()
		{
	    	
	    	 if( 0 == $scope.prevOrNextTopicPageNumCounter )
    		 {
	    		 $scope.PreviousTopicButtonFlag = true; // disabled
    		 }
	    	 else
    		 {
	    		 $scope.PreviousTopicButtonFlag = false; // enabled
    		 }
		}
		$scope.nextTopicClick=function()
		{
			$scope.prevOrNextTopicPageNumCounter ++;

			$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/topics',
				params:{
			
					currentPage: $scope.prevOrNextTopicPageNumCounter},
					
			
		     })
		     .success(function(response) 
		     {
		    	 $scope.NumOfTopicPages = response.numOfPages ;
		    	 $scope.CheckTopicNextButton();
		    	 $scope.CheckTopicPreviousButton();

		    	 
		    	 $scope.GetTopicsResult = angular.copy(response.topics);
		    	 
		    	 scroll(0,0);
			
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });
		}
		
		$scope.previousTopicClick = function()
		{
			$scope.prevOrNextTopicPageNumCounter--;
		
			$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/topics',
				params:{
					currentPage: $scope.prevOrNextTopicPageNumCounter, 
					newOrAll: "all"},
					
				
		     })
		     .success(function(response) 
		     {
		    	 $scope.NumOfTopicPages = response.numOfPages ;
		    	 
		    	 $scope.CheckTopicNextButton();
		    	 $scope.CheckTopicPreviousButton();
		    	 
		    	 $scope.GetTopicsResult = angular.copy(response.topics);
		    	 
		    	 scroll(0,0);
			   
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });
		}
		
		// Code start from here
	    $scope.NextTopicButtonFlag = true;
	    $scope.PreviousTopicButtonFlag = true;
		$scope.CheckSession();
		$scope.showHideTopicList = true;
		$scope.topicCurrentPage = 0;
		$scope.displayListTopics();
		$scope.showAns = false;
	    $scope.NextButtonFlag = true;
	    $scope.PreviousButtonFlag = true;
		$scope.AnswerShowFlag = false;
		$scope.prevOrNextPageNumCounter = 0;
		$scope.prevOrNextTopicPageNumCounter = 0;
		$scope.GetQuestionsResult = [];
		
}]);