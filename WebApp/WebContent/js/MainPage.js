angular.module('inTableApp',[])
	.controller('mainPageController',['$scope','$http', function($scope, $http) {
		
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
		
		$scope.addAnswer = function(obj)
		{
	
			alert(obj.answerTxt);
			alert(obj.questionId);
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
			    	 alert("hare2");
			    
			     })
			     .error(function (error) 
			     {
			 
			     }); 
		}

	    $scope.voteUp = function(obj){
	    	alert(obj.questionId);
			$http({ method: 'PUT',
		        url: 'http://localhost:8080/WebApp/questions/'+obj.questionId,
		        data: { questionVote: +1},
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}
		     })
		     .success(function(result) 
		     {
		    	 alert("updated vote");
		    	 obj.questionVote++;
		    	 
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     }); 
	    }
	    $scope.voteDown = function(obj){
	    	alert(obj.questionId);
			$http({ method: 'PUT',
		        url: 'http://localhost:8080/WebApp/questions/'+obj.questionId,
		        data: { questionVote: -1},
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}
		     })
		     .success(function(result) 
		     {
		    	 alert("updated vote");
		    	 obj.questionVote++;
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     }); 
	    }

		$scope.GetQuestions = function()
		{
			$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/questions',
				params: null,
				
		     })
		     .success(function(response) 
		     {
		    	 $scope.NumOfPages = response.numOfPages ;
		    	 $scope.CheckNextButton();
		    	 $scope.CheckPreviousButton();
		    	 
		    	 $scope.PreviousButtonFlag = true; // disable previous button
		    	 
		    	 $scope.GetQuestionsResult = angular.copy(response.questions);
		    	 //alert(JSON.stringify(response.questions[0].questionTopics));
		       /* for( i =0 ; i < response.questions.length ; i++)
	        	{
		        	$scope.GetQuestionsResult.push(response.questions[i]);
		        	//alert(response[i]);
	        	}
	        	*/
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });  
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
		           //alert('Resistration was successful');
		        	window.location.assign("index.html");
		         }
		         else
	        	 {
		        	 $scope.NickName = response.Nickname;
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
		
		
		$scope.nextClick=function()
		{
			$scope.prevOrNextPageNumCounter ++;
			//alert($scope.prevOrNextPageNumCounter);
			$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/questions',
				params:{
					//prevOrNext: "prev",
					currentPage: $scope.prevOrNextPageNumCounter
					
				},
		     })
		     .success(function(response) 
		     {
		    	 $scope.NumOfPages = response.numOfPages ;
		    	 $scope.CheckNextButton();
		    	 $scope.CheckPreviousButton();

		    	 
		    	 $scope.GetQuestionsResult = angular.copy(response.questions);
		    	 
		    	 scroll(0,0);
			        /*for( i =0 ; i < response.length ; i++)
		        	{
			        	$scope.GetQuestionsResult.push(response[i]);
			        	//alert(response[i]);
		        	}*/
		     })
		     .error(function (error) 
		     {
		             $scope.status = 'Unable to connect' + error.message;
		     });
		}
		
		$scope.previousClick = function()
		{
			$scope.prevOrNextPageNumCounter--;
			//alert($scope.prevOrNextPageNumCounter);
			$http({ method: 'GET',
		        url: 'http://localhost:8080/WebApp/questions',
				params:{
					//prevOrNext: "prev",
					currentPage: $scope.prevOrNextPageNumCounter
					
				},
		     })
		     .success(function(response) 
		     {
		    	 $scope.NumOfPages = response.numOfPages ;
		    	 
		    	 $scope.CheckNextButton();
		    	 $scope.CheckPreviousButton();
		    	 
		    	 $scope.GetQuestionsResult = angular.copy(response.questions);
		    	 
		    	 scroll(0,0);
			        /*for( i =0 ; i < response.length ; i++)
		        	{
			        	$scope.GetQuestionsResult.push(response[i]);
			        	//alert(response[i]);
		        	}*/
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
	        	
	         	if ($scope.tag.indexOf($scope.topics.substring(0, $scope.topics.length-1 )) == -1){  //push topic if it does not exist
	       	     
	        		$scope.tag.push( $scope.topics.substring(0, $scope.topics.length-1 ) );
	        	}
	        	
	        	$scope.topics = "";
	        }
	    };
		

		
		// code start from here when page is loading
	    $scope.NextButtonFlag = false;
	    $scope.PreviousButtonFlag = false;
	    $scope.HideShowTagCharacterError = false;
	    $scope.tag = [];
	    $scope.prevOrNextPageNumCounter = 0;
	    $scope.topicList;
		$scope.GetQuestionsResult =[];
		$scope.NumOfPages = 0;
		$scope.CheckSession();
			

		$scope.GetQuestions();
		
		
		
		
	}]);