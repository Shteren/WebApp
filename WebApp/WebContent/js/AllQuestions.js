var app = angular.module('MainApp',[]);
	app.directive('questions', function(){
		return {
			templateUrl: "questionstemplate.html",
		};
	});

app.controller('AllQuestionsController',['$scope','$http', function($scope, $http) {
	var url = window.location.pathname.split('/')[2];
	
		$scope.CheckNextButton = function()
		//function disables or enables the "next" button of the questions thread list 
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
				//function disables or enables the "prev" button of the questions thread list
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
		//function asks the server for questions according to the page we currently in 
		//if we are in home page- wee need only new questions, all questions if we are in "all questions" page and by topic in topic's page 
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

		    	 $scope.NumOfPages = response.numOfPages ;//num of question pages- 20 questions each
		    	 if (response.Result=="There is no result for this topic"){
		    		 alert(response.Result);
		    		 $scope.showList();
		    		 $scope.topic="";
		    		 return;
		    	 }
		    	 if ($scope.NumOfPages < $scope.prevOrNextPageNumCounter ){ //if we are in main page, and we answered a 
		    		 //question which is single in a page we need to get the previous page instead refreshing the current one 
		    		 $scope.prevOrNextPageNumCounter--;
		    		 $scope.GetAllQuestions(); 
		    	 } 
		    	 if ($scope.NumOfPages == -1) {//if list is empty no need to show the questions
		    		 $scope.showPrevNext = false;
		    	 } else {
		    		 $scope.showPrevNext = true;
		    	 }
		    	 $scope.CheckNextButton();
		    	 $scope.CheckPreviousButton();
		    	 $scope.GetQuestionsResult = angular.copy(response.questions); //array with all the questions of curren page
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });  
		}
		
		
		$scope.GetQuestionsAns = function(question ,show)
		//function asks the server for all questions of a specific answer
		//input : a question to get all its answers and a flag saying if the answers are open or not
		{
			//if questions are currently open and we clicked on the button or we want it to be closed
			if (((question.showAns == true) && (show == true) ) ||(show == false)){ 
				question.showAns = false;
				
			}else{
				question.showAns = true;
			}
			{
				$http({ method: 'GET',
			        url: 'http://localhost:8080/WebApp/questions/'+question.questionId +'/answers',
			     })
			     .success(function(response) 
			     {
			    	 if(null == response || 0 == response.length) //no answers to this question
			    	 {
			    		 question.firstAns=false;
			    		 return;
			    	 }
			    	 
			    	 question.firstAns=true;//there is at least one answer
			    	 question.numberOfAnswers = response.length;
			    	 $scope.ShowButton(question); //check if there are more then one question - if so - show button to get more answers
			    	 question.firstAns = response.slice(0, 1);//only first answer
			    	 question.GetAnswerResults = response.slice(1, response.length);//the rest of the answers
			     })
			     .error(function (error) 
			     {
			             $scope.status = 'Unable to connect' + error.message;
			     });  
			}
			
		}
	
		$scope.CheckSession = function()
		//check if there is a user connected, if not go to login and registration page
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
		    //go to login and register in order to log in with other user +disconnect session
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
			//function checks if there are more then one question in order to determine 
			//whether to show or not the "show more questions" button  
			if (obj.numberOfAnswers > 1)
			{
				obj.showButton = true;
			}
			else
			{
				obj.showButton = false;
			}
			
		}
		$scope.voteQuestionUp = function(obj)
		{
			//function updates question's votes according to users vote 
			$http({ method: 'PUT',
		        url: 'http://localhost:8080/WebApp/questions/'+obj.questionId,
		        data: { questionVote: +1},
				headers: {'Content-Type': 'application/json'}
		     })
		     .success(function(result) 
		     {
		    	 //if user is unable to vote, display appropriate message 
		    	 if ((result.Result == "It's your question")||(result.Result == "The user already voted")){
		    		 alert(result.Result); 
		    	 }else{
		    		obj.questionVote++; 
		    	 }
		    	 //update questions as question location may have changed
		    	 $scope.GetAllQuestions();
		    	 
		    	 
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     }); 
	    }
		
		$scope.voteQuestionDown = function(obj)
		{	
			//function updates question's votes according to users vote as described above in function "voteQuestionUp"
			$http({ method: 'PUT',
		        url: 'http://localhost:8080/WebApp/questions/'+obj.questionId,
		        data: { questionVote: -1},
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}
		     })
		     .success(function(result) 
		     {
		    	 if ((result.Result == "It's your question")||(result.Result == "The user already voted")){
		    		 alert(result.Result); 
		    	 }else{
		    		obj.questionVote--; 
		    	 }
		    	 $scope.GetAllQuestions();
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     }); 
	    }
	    $scope.voteAnsDown = function(answer, question){
	    	//function updates answer's votes according to users vote
			$http({ method: 'PUT',
		        url: 'http://localhost:8080/WebApp/answers/'+answer.answerId,
		        data: { answerVote: -1},
				headers: {'Content-Type': 'application/json'}
		     })
		     .success(function(result) 
		     {
		    	//if user is unable to vote, display appropriate message 
		    	 if ((result.Result == "It's your answer")||(result.Result == "The user already voted")){
		    		 alert(result.Result); 
		    		 return;
		    	 }else{
		    		 if (null==question){//nothing to update
		    			return;
		    		 }
		    		 answer.answerVote--; 
		    		 //if list is open this will make it not close - like i did not press the show button
		    		 if (question.showAns==true){
		    			 question.showAns=false;
		    		 } 
		    		//update answers as answers location may have changed
		    		 $scope.GetQuestionsAns(question ,true);
		    	 }
		    	 
		     })
		     .error(function (error) 
		     {
		    	 alert("voteDown");
		             $scope.status = 'Unable to connect' + error.message;
		     }); 
	    }
	    
		$scope.voteAnsUp = function(answer, question){
			//function updates answer's votes according to users vote as described above in function "voteAnsUp"	    	
			$http({ method: 'PUT',
		        url: 'http://localhost:8080/WebApp/answers/'+answer.answerId,
		        data: { answerVote: +1},
				headers: {'Content-Type': 'application/json'}
		     })
		     .success(function(result) 
		     {
		    	 
		    	 if ((result.Result == "It's your answer")||(result.Result == "The user already voted")){
		    		 alert(result.Result); 
		    	 }else{
		    		 if (null == question) {
		    			 return;
		    		 }
		    		 answer.answerVote++; 
		    		 if (question.showAns==true){
		    			 question.showAns=false;
		    		 } 
		    		 $scope.GetQuestionsAns(question ,true);
		    		
		    		 }
		     })
		     .error(function (error) 
		     {
		    	 alert("voteUp")
		             $scope.status = 'Unable to connect' + error.message;
		     }); 
	    }
	    
	    
	    $scope.timeFormat = function(obj)
	    {
	    	//input : question or answer 
	    	//output: its submission time according to the requirements 
	    	if (obj == null) {
	    		return;
	    	}
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
		//load next 20 questions
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
		    	 
		    	 scroll(0,0);//move back to top
			
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });
		}
		
		$scope.previousClick = function()
		//load previous 20 questions as in "nextClick"
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
		
		$scope.addAnswer = function(question)
		//add an answer to a specific question
		{
			 $http({ method: 'POST',
			        url: 'http://localhost:8080/WebApp/answers',
					data: {
						answerTxt: question.answerTxt ,
						questionId: question.questionId
					},
					headers: {'Content-Type': 'application/json'}
			     })
			     
			     .success(function(response) 
			     {
			    	 question.sentAnswerTxt = question.answerTxt;
			    	 question.answerTxt = null;
			    	  $scope.GetQuestionsAns(question);//refresh the answers of the question in order for the 
			    	  //answer to go in to its right place 
			    	
			    	  
			    
			     })
			     .error(function (error) 
			     {
			 
			     }); 
		}
		
		$scope.QuestionLenWarn = function()
		//warn if question exceed 300 characters 
	    {
	    	if ( ($scope.questionTxt.length)>300)
	    	{
	    		$scope.HideShowQuestionCharacterError = true;
	    		return;
	    	}
	    	$scope.HideShowQuestionCharacterError = false;
	    }
		
	    $scope.AnswerLenWarn = function(question)
	    //warn if answer exceed 300 characters
	    //input- the question the answer text box belongs to
	    {
	    	if (null == question) {
	    		return;
	    	}
	    	if ( (question.answerTxt.length)>300)
	    	{
	    		question.ansLenShowErr = true;
	    		return;
	    	}else{
	    		question.ansLenShowErr = false;
	    		return;
	    	}
	    	
	    
	    }
	    
	    $scope.focusTxt = function(){
	    	//set "answerOpen" to true if there is a focused text box
	    	$scope.answerOpen = true;
	    }
	    $scope.blurTxt = function(){
	    	//set "answerOpen" to false the text box which was focused is blurred now
	    	$scope.answerOpen = false;
	    }
	    
	    $scope.selectPage = function() 
	    {
	    	//init page according to the one we are in 
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
			//if no focused text boxes it is safe to refresh
			if ($scope.answerOpen == false){
				$scope.GetAllQuestions();
			}
		}
	    
	    // functions only for newQuestions page 
	    $scope.initNewQuestionsPage = function() 
	    {
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
	   	    	 //init the boxes and variables to be ready for new questions
	   	    		$scope.questionTxt = "";
	   		    	$scope.tag=[];
	   		    	$scope.topics = "";
	   		
	   		        //refresh all questions to show the question
	   		    	$scope.GetAllQuestions();
	   	     })
	   	     .error(function (error) 
	   	     {
	   	             $scope.status = 'Unable to connect' + error.message;
	   	     }); 
   	}
   	
	   	$scope.tagMaker = function() 
	   	{
	   		//function checks if tag is not longer the 50 chars, and turns each input 
	   		//terminated by comma in to a tag, then makes a list of tags
	   		
	   		
	   		if( ($scope.topics.length) > 50)
	   		{
	   			$scope.HideShowTagCharacterError = true;
	   			return;
	   	    }
		
	   		$scope.HideShowTagCharacterError = false;
	   		//if last character is comma, insert the topic to the tags array and clear the textbox
	   		if ($scope.topics[ $scope.topics.length -1 ] == ",")
	   		{
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
			//if questions are displayed, the search for topics option should disappear, instead there is a back button
			$scope.showHideTopicList = true;
		}
	    
	    $scope.displayListTopics = function(){
	    	//display 20 topics each time, each topic is a button leading to its answers
	    	$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/topics',
					params: {currentPage:$scope.prevOrNextPageNumCounter
					},
		     })
		     .success(function (response) 
		     {;
		    	 $scope.NumOfTopicPages = response.numOfPages ;
		    	 $scope.CheckTopicNextButton();
		    	 $scope.CheckTopicPreviousButton();
		    	 $scope.GetTopicsResult = angular.copy(response.topics);
		        
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });  
			return false;
	    	
	    }
	    
	    $scope.CheckTopicNextButton = function()
		{
	    	//disable if there are no topics in next page
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
			//disable if there are no topics in previous page
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
			//get the questions for the next page
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
		    	 //scroll to top of the list
		    	 scroll(0,0);
			
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });
		}
		
		$scope.previousTopicClick = function()
		{
			//get questions for the previous page
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
		
		$scope.setTopic = function(top)
		//function receives a topic , gets all questions and makes the search for topics disappear
		{
			$scope.topic =  top;
			$scope.GetAllQuestions();
			$scope.showHideTopicList = false;
		}
		$scope.setTopicSearch = function(){
			if ($scope.topic!=null){
				$scope.GetAllQuestions();
				$scope.showHideTopicList = false;
			}
		}
		
		// functions for all questions
		$scope.initAllQuestionsPage = function()
		{
			$scope.GetAllQuestions();
			
		}
		
		// Code start from here
		
		//init params
	    $scope.tag = [];
	    $scope.answerOpen=0;
		$scope.showAns = false;
	    $scope.NextButtonFlag = true;
	    $scope.PreviousButtonFlag = true;
		$scope.AnswerShowFlag = false;
		$scope.sendTopic = null;
		$scope.showHideTopicList = true;
	    $scope.HideShowTagCharacterError = false;	    
		$scope.prevOrNextPageNumCounter = 0;
		$scope.prevOrNextTopicPageNumCounter = 0;
		$scope.GetQuestionsResult = [];
		$scope.TopicToSearch=null;		
		$scope.sendNewOrAll = "new";
	    $scope.showPrevNext = true;
	    //initiation functions
	    $scope.selectPage();
	    $scope.CheckSession();
		
		
}]);
