var app = angular.module('MainApp',[]);
	app.directive('questions', function(){
		return {
			templateUrl: "questionstemplate.html",
		};
	});

app.controller('AllQuestionsController',['$scope','$http', function($scope, $http) {
	var url = window.location.pathname.split('/')[2];


	

		$scope.CheckNextButton = function()
		{

			if( $scope.NumOfPages == $scope.prevOrNextPageNumCounter)
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
			
			if (url == "AllQuestions.html"){
				$scope.sendNewOrAll="all";
			}
			if (url == "QuestionsByTopic.html"){
				$scope.sendTopic=$scope.topic;
			}
			$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/questions',
				params: { 	currentPage: $scope.prevOrNextPageNumCounter, 
							newOrAll: $scope.sendNewOrAll,
							topicName:$scope.sendTopic},
				
		     })
		     .success(function(response) 
		     {

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
			    		 obj.firstAns=false;
			    		 return;
			    	 }
			    	 obj.start0=true; //refresh is enabled , answer box is empty
			    	 obj.firstAns=true;
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
		//ToDo can we delete it?
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
				headers: {'Content-Type': 'application/json'}
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
		    	 if ((result.Result == "It's your answer")||(result.Result == "The user already vote")){
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
			if (url == "AllQuestions.html"){
				$scope.sendNewOrAll="all";
			}
			if (url == "QuestionByTopic.html"){
				$scope.sendTopic=$scope.topic;
			}
			$scope.prevOrNextPageNumCounter ++;

			$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/questions',
				params:{
			
					currentPage: $scope.prevOrNextPageNumCounter, 
					newOrAll: $scope.sendNewOrAll,
					topicName: $scope.sendTopic},
					
			
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
			if (url == "AllQuestions.html"){
				$scope.sendNewOrAll="all";
			}
			if (url == "QuestionByTopic.html"){
				$scope.sendTopic=$scope.topic;
			}
			$scope.prevOrNextPageNumCounter--;
		
			$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/questions',
				params:{
					currentPage: $scope.prevOrNextPageNumCounter, 
					newOrAll: $scope.sendNewOrAll,
					topicName: $scope.sendTopic
					},
					
				
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
			    	  $scope.GetQuestionsAns(obj);
			    	  obj.showAns = false;
			    	  
			    
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
	    	if (( (obj.answerTxt.length)==1) & ((obj.start0)==true)){ //if one char and started ro 0
	    		obj.answerOpen = 1;
	    		obj.start0=false; //when come back to ne char next time will not add 1
	    	}else if ( (obj.answerTxt.length)==0){
	    		obj.answerOpen = -1;
	    		obj.start0=true; //it is first char again
	    	}
	    	else{
	    		obj.answerOpen = 0;
	    	}
	    	$scope.answerOpen += obj.answerOpen;
	    }
	    
	    
	    $scope.selectPage = function() 
	    {
			if (url == "AllQuestions.html"){
				$scope.initAllQuestionsPage();
			}
			else if (url == "QuestionsByTopic.html"){
				$scope.initTopicsPage();
			} else {
				$scope.initNewQuestionsPage();
			}
	    }
	    
		// refresh condition 
		$scope.Newquestions = function ()
		{
			if ($scope.answerOpen == 0){
				$scope.GetAllQuestions();
			}
		}
	    
	    // functions only for newQuestions page 
	    $scope.initNewQuestionsPage = function() 
	    {
	    	//$scope.addQuestion();
	    
	    	//$scope.Newquestions();
	    	$scope.GetAllQuestions();	
	    	
			//refresh every 2.5 sec
			setInterval($scope.Newquestions, 4500);
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
	   	    $scope.topicList = $scope.tag;
	   	    
	   	   
	   		$http({ method: 'POST',
	   	        url: 'http://localhost:8080/WebApp/questions',
	   			data: {
	   				questionTxt: $scope.questionTxt,
	   				questionTopics: $scope.topicList
	   			},
	   			headers: {'Content-Type': 'application/json'}
	   	     })
	   	     .success(function(response) 
	   	     {
	   	    		$scope.questionTxt = "";
	   		    	$scope.tag=[];
	   		    	$scope.topics = "";
	   		
	   		        
	   		        $scope.GetQuestions();
	   	     })
	   	     .error(function (error) 
	   	     {
	   	             $scope.status = 'Unable to connect' + error.message;
	   	     }); 
   	}
   	
	   	$scope.tagMaker = function() 
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
       	   alert("topicsMaker");
           	if ($scope.tag.indexOf($scope.topics.substring(0, $scope.topics.length-1 )) == -1){  //push topic if it does not exist
         	     
          		$scope.tag.push( $scope.topics.substring(0, $scope.topics.length-1 ) );
          	}
          	
          	$scope.topics = "";
          }
      }
	    
	    // functions only for Topic page 
	    $scope.initTopicsPage = function()
	    {

	    	$scope.showList();
	    	$scope.displayListTopics();

	    }
	    
		$scope.showList = function()
		{
			$scope.showHideTopicList = true;
		}
	    
	    $scope.displayListTopics = function(){
	    	$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/topics',
					params: {currentPage:$scope.prevOrNextPageNumCounter
					},
		     })
		     .success(function (response) 
		     {;
		    	 $scope.NumOfTopicPages = response.numOfPages ;
		    	// alert($scope.NumOfTopicPages);
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
		
		$scope.setTopic = function(obj)
		{
			$scope.topic =  obj;
			$scope.GetAllQuestions();
			$scope.showHideTopicList = false;
		}
		
		// functions for all questions
		$scope.initAllQuestionsPage = function()
		{
			$scope.GetAllQuestions();
			
		}
		
		// Code start from here
	    $scope.tag = [];
	    $scope.answerOpen=0;
		$scope.showAns = false;
	    $scope.NextButtonFlag = true;
	    $scope.PreviousButtonFlag = true;
		$scope.AnswerShowFlag = false;
		$scope.sendTopic = null;
	    //$scope.initTopicsPage();
	  //  $scope.initNewQuestionsPage();
	   // $scope.initAllQuestionsPage();
	    $scope.selectPage();
		$scope.showHideTopicList = true;
	    $scope.HideShowTagCharacterError = false;	    
		$scope.prevOrNextPageNumCounter = 0;
		$scope.GetQuestionsResult = [];
		$scope.TopicToSearch=null;
		//default values for get questions		
		$scope.sendNewOrAll = "new";
	    $scope.CheckSession();
	    //$scope.GetAllQuestions();
		
		
}]);
