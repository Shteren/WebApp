angular.module('inAllQuestion',[]).controller('AllQuestionsController',['$scope','$http', function($scope, $http) {
		$scope.CheckNextButton = function()
		{

			if( $scope.NumOfPages == $scope.prevOrNextPageNumCounter+1 )
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
		
		$scope.GetAllQuestions = function()
		{
	
			$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/questions',
				params: { 	currentPage: $scope.prevOrNextPageNumCounter, 
							newOrAll: "all"},
				
		     })
		     .success(function(response) 
		     {

		    	 $scope.NumOfPages = response.numOfPages ;
		    	 $scope.CheckNextButton();
		    	 $scope.CheckPreviousButton();
		    	 
		    	 $scope.PreviousButtonFlag = true; // disable previous button
		    	 
		    	 $scope.GetQuestionsResult = angular.copy(response.questions);
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });  
		}
		
		
		$scope.GetQuestionsAns = function(obj ,show)
		{
			if (((obj.showAns == true) && (show == true) ) ||(show == false)){
				$scope.falseShow(obj)
				
			}else{
				$scope.trueShow(obj)
			}
			{
				$http({ method: 'GET',
			        url: 'http://localhost:8080/WebApp/questions/'+obj.questionId +'/answers',
			     })
			     .success(function(response) 
			     {
			    	 if(null == response || 0 == response.length)
			    	 {
			    		 alert("Not Found");
			    		 return;
			    	 }
			
			    	 obj.numberOfAnswers = response.length;
			    	 $scope.ShowButton(obj);
			    	 obj.firstAns = response.slice(0, 1);
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
		$scope.trueShow = function(obj)
		{
			obj.showAns = true;
			
		}
		
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
		$scope.ShowButton = function(obj)
		{

			if (obj.numberOfAnswers > 1)
			{
				obj.showButton = true;
			}
			else
			{
				obj.showButton = false;
			}
			
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
	    $scope.voteAnsDown = function(obj){
	    	
			$http({ method: 'PUT',
		        url: 'http://localhost:8080/WebApp/answers/'+obj.questionId,
		        data: { answerVote: -1},
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}
		     })
		     .success(function(result) 
		     {
		    	 if ((result.Result == "It's your question")||(result.Result == "The user already vote")){
		    		 alert(result.Result); 
		    	 }else{
		    		obj.answerVote--; 
		    	 }
		     })
		     .error(function (error) 
		     {
		    	 alert("voteDown");
		             $scope.status = 'Unable to connect' + error.message;
		     }); 
	    }
		$scope.voteAnsUp = function(obj){
	    	
			$http({ method: 'PUT',
		        url: 'http://localhost:8080/WebApp/answers/'+obj.answerId,
		        data: { answerVote: +1},
				headers: {'Content-Type': 'application/json'}
		     })
		     .success(function(result) 
		     {
		    	 
		    	 if ((result.Result == "It's your answer")||(result.Result == "The user already vote")){
		    		 alert(result.Result); 
		    	 }else{
		    		obj.answerVote++; 
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
		$scope.nextClick=function()
		{
			$scope.prevOrNextPageNumCounter ++;

			$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/questions',
				params:{
			
					currentPage: $scope.prevOrNextPageNumCounter, 
					newOrAll: "all"},
					
			
		     })
		     .success(function(response) 
		     {
		    	 $scope.NumOfPages = response.numOfPages ;
		    	 $scope.CheckNextButton();
		    	 $scope.CheckPreviousButton();

		    	 
		    	 $scope.GetQuestionsResult = angular.copy(response.questions);
		    	 
		    	 scroll(0,0);
			
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });
		}
		
		$scope.previousClick = function()
		{
			$scope.prevOrNextPageNumCounter--;
		
			$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/questions',
				params:{
					currentPage: $scope.prevOrNextPageNumCounter, 
					newOrAll: "all"},
					
				
		     })
		     .success(function(response) 
		     {
		    	 $scope.NumOfPages = response.numOfPages ;
		    	 
		    	 $scope.CheckNextButton();
		    	 $scope.CheckPreviousButton();
		    	 
		    	 $scope.GetQuestionsResult = angular.copy(response.questions);
		    	 
		    	 scroll(0,0);
			   
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });
		}
		
		$scope.addAnswer = function(obj)
		{
	
			
			 $http({ method: 'POST',
			        url: 'http://localhost:8080/WebApp/answers',
					data: {
						answerTxt: obj.answerTxt ,
						questionId: obj.questionId
					},
					headers: {'Content-Type': 'application/json'}
			     })
			     
			     .success(function(response) 
			     {
			    	  obj.sentAnswerTxt = obj.answerTxt;
			    	  obj.answerTxt = null;
			    
			     })
			     .error(function (error) 
			     {
			 
			     }); 
		}
		
		$scope.QuestionLenWarn = function()
	    {
	    	if ( ($scope.questionTxt.length)>300)
	    	{
	    		$scope.HideShowQuestionCharacterError = true;
	    		return;
	    	}
	    	$scope.HideShowQuestionCharacterError = false;
	    }
	    $scope.QuestionLenWarn = function(obj)
	    {

	    	if ( (obj.answerTxt.length)>300)
	    	{
	    		obj.ansLenShowErr = true;
	    		return;
	    	}
	    	obj.ansLenShowErr = false;
	    	
	    }
		
		// Code start from here
		$scope.showAns = false;
	    $scope.NextButtonFlag = true;
	    $scope.PreviousButtonFlag = true;
		$scope.AnswerShowFlag = false;
		$scope.prevOrNextPageNumCounter = 0;
		$scope.GetQuestionsResult = [];
		
	    $scope.CheckSession();
	    $scope.GetAllQuestions();
		

		
}]);