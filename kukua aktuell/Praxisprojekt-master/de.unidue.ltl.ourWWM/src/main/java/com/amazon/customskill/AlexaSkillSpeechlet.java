/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.amazon.customskill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;


/*
 * This class is the actual skill. Here you receive the input and have to produce the speech output. 
 */
public class AlexaSkillSpeechlet
implements SpeechletV2
{
	static Logger logger = LoggerFactory.getLogger(AlexaSkillSpeechlet.class);

	public static String userRequest;

	private static int sum;
	private static int sum2;
	private static int sum3;
	private static int sum4;
	private static int sum5;
	private static int questions;
	private static int questions2;
	private static int questions3;
	private static String question = "";
	private static String question2 = "";
	private static String question3 = "";
	private static String correctAnswer = "";
	private static String correctAnswer2 = "";
	private static String correctAnswer3 = "";
	private static enum RecognitionState {Answer, AnswerTwo, AnswerThree, AnswerFour, AnswerFive, YesNo, YesNoTwo, YesNoLevel, OneTwo, VokabelQuiz, Vokabel, WhichPlayer, WhichPlayerThree, Weiterquizzen, SingleQuiz, YesNoQuiz, YesNoVokabeln, AnswerVokabeln, AnswerQuiz};
	private RecognitionState recState;
	private static enum UserIntent {menü, bye, playerone, one, two, playertwo, vokabeln, quiz, einer, mehrere, weiter, ja, aufhören, beenden, nein, hello, tree, light, now, maybe, today, einfach, mittel, schwer, moin, nextlevel, Error, Quiz};
	UserIntent ourUserIntent;

	static String welcomeMsg = "Hallo und herzlich willkommen bei Quizzitch. Möchten einer oder mehrere Spieler spielen?";
	static String singleMsg = "Sie sind im Einzelspielermodus. Möchten Sie Vokabeln lernen oder quizzen?";
	static String multiMsg = "Sie sind im Mehrspielermodus. Einigen Sie sich nun, wer Spieler 1 und wer Spieler 2 ist. Wenn Sie die Antwort auf die Frage kennen, rufen Sie Ihre Spielernummer. Ist die Antwort korrekt, erhalten Sie Punkte. Los geeeehts!";
	static String difficultyMsg = "Schwierigkeit einfach, mittel oder schwer?";
	static String singleQuizMsg = "Sie sind im Einzelquiz. Sind sie bereit zu quizzen?";
	static String wrongMsg = "Das ist leider falsch.";
	static String correctMsg = "Das ist richtig.";
	static String continueMsg = "Möchten Sie weiter machen?";
	static String congratsMsg = "Herzlichen Glückwunsch! Sie haben eine Million Punkte gewonnen.";
	static String goodbyeMsg = "Auf Wiedersehen!";
	static String sumMsg = "Sie haben {replacement} Punkte.";
	static String sumTwoMsg = "Spieler eins hat {replacement3} Punkte.";
	static String sumThreeMsg = "Spieler zwei hat {replacement5} Punkte.";
	static String errorYesNoMsg = "Das habe ich nicht verstanden. Sagen Sie bitte weiter oder aufhören.";
	static String errorAnswerMsg = "Das habe ich nicht verstanden. Sagen Sie bitte erneut Ihre Antwort.";
	static String errorOneTwoMsg = "Das habe ich nicht verstanden. Sagen Sie bitte einer oder zwei.";
	static String errorVokabelQuizMsg = "Das habe ich nicht verstanden. Sagen Sie bitte Vokabeln oder Quiz.";
	static String errorVokabelMsg = "In welcher Schwierigkeitsstufe möchten Sie Vokabeln üben? Sagen Sie bitte einfach, mittel oder schwer.";
	static String VokabelLeicht = "Sie sind im leichten Vokabeltrainermodus";
	static String VokabelMittel = "Sie sind im mittleren Vokabeltrainermodus";
	static String VokabelSchwer = "Sie sind im schweren Vokabeltrainermodus";
	static String errorSpielereinszweiMsg = "Welcher Spieler weiß die Antwort?";
	static String SpielerEinsMsg = "Spieler eins war schneller. wie lautet die Antwort?";
	static String SpielerZweiMsg = "Spieler zwei war schneller. wie lautet die Antwort?";
	static String continueLevelMsg = "Weiter gehts in Level zwei. Möchten Sie weiterspielen?";
	static String playerOneWins = "Spieler eins gewinnt die Runde.";
	static String playerTwoWins = "Spieler zwei gewinnt die Runde.";
	static String weiterquizzenMsg = "Möchten Sie nun etwas Quiz spielen oder beenden?";
	static String errorWeiterquizzen = "Das habe ich nicht verstanden. Sagen Sie bitte Quiz oder beenden.";
	static String weiterVokabelnMsg = "Möchten Sie nun etwas Vokabeln lernen oder beenden?";



	private String buildString(String msg, String replacement1, String replacement2) {
		return msg.replace("{replacement}", replacement1).replace("{replacement2}", replacement2);
	}
	
	private String buildString2(String msg, String replacement3, String replacement4) {
		return msg.replace("{replacement3}", replacement3).replace("{replacement4}", replacement4);
	}
	
	private String buildString3(String msg, String replacement5, String replacement6) {
		return msg.replace("{replacement5}", replacement5).replace("{replacement6}", replacement6);
	}



	@Override
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope)
	{
		logger.info("Alexa session begins");
		sum = 0;
		sum2 = 0;
		sum3 = 0;
		questions = 0;
		questions2 = 0;
		questions3 = 0;
		recState = RecognitionState.OneTwo;
	}

	@Override
	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope)
	{

		return askUserResponse(welcomeMsg);
		
	}



	private void selectQuestion() {
		switch(questions){
		case 0: question = "Hallo bedeutet auf englisch hello. Sage hallo auf englisch."; correctAnswer = "hello"; break;
		case 50: question = "baum bedeutet auf englisch tree. Sage baum auf englisch."; correctAnswer = "tree"; break;
		case 100: question = "jetzt bedeutet auf englisch now. Sage jetzt auf englisch."; correctAnswer = "now"; break;
		case 200: question = "vielleicht bedeutet auf englisch maybe. Sage vielleicht auf englisch."; correctAnswer = "maybe"; break;
		case 300: question = "heute bedeutet auf englisch today. Sage heute auf englisch."; correctAnswer = "today"; break;
		case 500: question = "Frage?"; correctAnswer = "today"; break;
		case 1000: question = "Frage?"; correctAnswer = "today"; break;
		case 2000: question = "Frage?"; correctAnswer = "today"; break;
		case 4000: question = "Frage?"; correctAnswer = "today"; break;
		case 8000: question = "Frage?"; correctAnswer = "today"; break;
		case 16000: question = "Frage?"; correctAnswer = "today"; break;
		case 32000: question = "Frage?"; correctAnswer = "today"; break;
		case 64000: question = "Frage?"; correctAnswer = "today"; break;
		case 125000: question = "Frage?"; correctAnswer = "today"; break;
		case 500000: question = "Frage?"; correctAnswer = "today"; break;
		}
	}
	
	private void selectQuestion2() {
		switch(questions2){
		case 0: question2 = "What's the opposite of dark? Who knows the answer?"; correctAnswer2 = "light"; break;
		case 50: question2 = "baum bedeutet auf englisch tree. Sage baum auf englisch. Welcher Spieler weiß die Antwort?"; correctAnswer2 = "tree"; break;
		case 100: question2 = "jetzt bedeutet auf englisch now. Sage jetzt auf englisch. Welcher Spieler weiß die Antwort?"; correctAnswer2 = "now"; break;
		case 200: question2 = "vielleicht bedeutet auf englisch maybe. Sage vielleicht auf englisch. Welcher Spieler weiß die Antwort?"; correctAnswer2 = "maybe"; break;
		case 300: question2 = "heute bedeutet auf englisch today. Sage heute auf englisch. Welcher Spieler weiß die Antwort?"; correctAnswer2 = "today"; break;
		case 500: question2 = "Frage?"; correctAnswer2 = "today"; break;
		case 1000: question2 = "Frage?"; correctAnswer2 = "today"; break;
		case 2000: question2 = "Frage?"; correctAnswer2 = "today"; break;
		case 4000: question2 = "Frage?"; correctAnswer2 = "today"; break;
		case 8000: question2 = "Frage?"; correctAnswer2 = "today"; break;
		case 16000: question2 = "Frage?"; correctAnswer2 = "today"; break;
		case 32000: question2 = "Frage?"; correctAnswer2 = "today"; break;
		case 64000: question2 = "Frage?"; correctAnswer2 = "today"; break;
		case 125000: question2 = "Frage?"; correctAnswer2 = "today"; break;
		case 500000: question2 = "Frage?"; correctAnswer2 = "today"; break;
		}
	}
	
	private void selectQuestion3() {
		switch(questions3){
		case 0: question3 = "moin moin bedeutet auf englisch moin moin. Sage moin moin auf englisch. Welcher Spieler weiß die Antwort?"; correctAnswer3 = "moin"; break;
		case 50: question3 = "baum bedeutet auf englisch tree. Sage baum auf englisch. Welcher Spieler weiß die Antwort?"; correctAnswer3 = "tree"; break;
		case 100: question3 = "jetzt bedeutet auf englisch now. Sage jetzt auf englisch. Welcher Spieler weiß die Antwort?"; correctAnswer3 = "now"; break;
		case 200: question3 = "vielleicht bedeutet auf englisch maybe. Sage vielleicht auf englisch. Welcher Spieler weiß die Antwort?"; correctAnswer3 = "maybe"; break;
		case 300: question3 = "heute bedeutet auf englisch today. Sage heute auf englisch. Welcher Spieler weiß die Antwort?"; correctAnswer3 = "today"; break;
		case 500: question3 = "Frage?"; correctAnswer3 = "today"; break;
		case 1000: question3 = "Frage?"; correctAnswer3 = "today"; break;
		case 2000: question3 = "Frage?"; correctAnswer3 = "today"; break;
		case 4000: question3 = "Frage?"; correctAnswer3 = "today"; break;
		case 8000: question3 = "Frage?"; correctAnswer3 = "today"; break;
		case 16000: question3 = "Frage?"; correctAnswer3 = "today"; break;
		case 32000: question3 = "Frage?"; correctAnswer3 = "today"; break;
		case 64000: question3 = "Frage?"; correctAnswer3 = "today"; break;
		case 125000: question3 = "Frage?"; correctAnswer3 = "today"; break;
		case 500000: question3 = "Frage?"; correctAnswer3 = "today"; break;
		}
	}

	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope)
	{
		IntentRequest request = requestEnvelope.getRequest();
		Intent intent = request.getIntent();
		userRequest = intent.getSlot("anything").getValue();
		logger.info("Received following text: [" + userRequest + "]");
		logger.info("recState is [" + recState + "]");
		SpeechletResponse resp = null;
		switch (recState) {
		/* case Answer: resp = evaluateAnswer(userRequest); break; */
		case OneTwo: resp = evaluateOneTwo(userRequest); break;
		case VokabelQuiz: resp = evaluateVokabelQuiz(userRequest); break;
		/* case YesNo: resp = evaluateYesNo(userRequest); break; */
		case WhichPlayer: resp = evaluateWhichPlayer(userRequest); break;
		case WhichPlayerThree: resp = evaluateWhichPlayerThree(userRequest); break;
		case Vokabel: resp = evaluateVokabel(userRequest); break;
		case AnswerTwo: resp = evaluateAnswerTwo(userRequest); break;
		case AnswerThree: resp = evaluateAnswerThree(userRequest); break;
		case AnswerFour: resp = evaluateAnswerFour(userRequest); break;
		case AnswerFive: resp = evaluateAnswerFive(userRequest); break;
		case YesNoTwo: resp = evaluateYesNoTwo(userRequest); break;
		case YesNoLevel: resp = evaluateYesNoLevel(userRequest); break;
		case Weiterquizzen: resp = evaluateWeiterquizzen(userRequest); break;
		case SingleQuiz: resp = evaluateSingleQuiz(userRequest); break;
		case YesNoQuiz: resp = evaluateYesNoQuiz(userRequest); break;
		case YesNoVokabeln: resp = evaluateYesNoVokabeln(userRequest); break;
		case AnswerVokabeln: resp = evaluateAnswerVokabeln(userRequest); break;
		case AnswerQuiz: resp = evaluateAnswerQuiz(userRequest); break;
		/*recState = RecognitionState.Answer; break;*/
		default: resp = response("Erkannter Text: " + userRequest);
		}   
		return resp;
	}

	/* Im Vokabelteil: Möchten Sie weitermachen? -> stattdessen Quizzen? */
	private SpeechletResponse evaluateYesNoVokabeln(String userRequest) {	
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case weiter: {
			selectQuestion();
			res = askUserResponse(question);
			recState = RecognitionState.AnswerVokabeln; break;
		} case ja: {
			selectQuestion();
			res = askUserResponse(question);
			recState = RecognitionState.AnswerVokabeln; break;
			
		} case aufhören: {
			res = askUserResponse(weiterquizzenMsg);
			recState = RecognitionState.Weiterquizzen; break;
		} case nein: {
			res = askUserResponse(weiterquizzenMsg);
			recState = RecognitionState.Weiterquizzen; break;
			
		} case menü: {
			res = askUserResponse(welcomeMsg);
			recState = RecognitionState.OneTwo; break;
		} default: {
			res = askUserResponse(errorYesNoMsg);
		}
		}
		return res;
	}
	
	
	/* Im Quizteil: Möchten Sie weitermachen? -> stattdessen Vokabeln? */
	
	private SpeechletResponse evaluateYesNoQuiz(String userRequest) {	
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case weiter: {
			selectQuestion();
			res = askUserResponse(question);
			recState = RecognitionState.AnswerQuiz; break;
		} case ja: {
			selectQuestion();
			res = askUserResponse(question);
			recState = RecognitionState.AnswerQuiz; break;
			
		} case aufhören: {
			res = askUserResponse(weiterVokabelnMsg);
			recState = RecognitionState.Vokabel; break;
		} case nein: {
			res = askUserResponse(weiterVokabelnMsg);
			recState = RecognitionState.Vokabel; break;
			
		} case menü: {
			res = askUserResponse(welcomeMsg);
			recState = RecognitionState.OneTwo; break;
			
		} default: {
			res = askUserResponse(errorYesNoMsg);
		}
		}
		return res;
	}
	
	
	private SpeechletResponse evaluateWeiterquizzen(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case quiz: {
			res = askUserResponse(singleQuizMsg);
			recState = RecognitionState.SingleQuiz; break;
		} case beenden: {
			/*recState = RecognitionState.WhichPlayer;*/
			res = response(/*buildString(sumMsg, String.valueOf(sum), "")+" "+*/goodbyeMsg); break;
		} default: {
			res = askUserResponse(errorWeiterquizzen);
		}
		}
		return res;
	}
	
	
	
	
	private SpeechletResponse evaluateYesNoTwo(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case weiter: {
			selectQuestion2();
			res = askUserResponse(question2);
			recState = RecognitionState.WhichPlayer; break;
		} case aufhören: {
			/*recState = RecognitionState.WhichPlayer;*/
			res = response(/*buildString(sumMsg, String.valueOf(sum), "")+" "+*/goodbyeMsg); break;
		} case menü: {
			res = askUserResponse(welcomeMsg);
			recState = RecognitionState.OneTwo; break;
		} default: {
			res = askUserResponse(errorYesNoMsg);
		}
		}
		return res;
	}
	
	private SpeechletResponse evaluateYesNoLevel(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case weiter: {
			selectQuestion3();
			res = askUserResponse(question3);
			recState = RecognitionState.WhichPlayerThree; break;
		} case aufhören: {
			/*recState = RecognitionState.WhichPlayer;*/
			res = response(/*buildString(sumMsg, String.valueOf(sum), "")+" "+*/goodbyeMsg); break;
		} case menü: {
			res = askUserResponse(welcomeMsg);
			recState = RecognitionState.OneTwo; break;
		} default: {
			res = askUserResponse(errorYesNoMsg);
		}
		}
		return res;
	}
	
	private SpeechletResponse evaluateOneTwo(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case einer: {
			res = askUserResponse(singleMsg);
			recState = RecognitionState.VokabelQuiz; break;
		} case mehrere: {
			selectQuestion2();
			res = askUserResponse(multiMsg+" "+question2);
			recState = RecognitionState.WhichPlayer; break;
		} default: {
			res = askUserResponse(errorOneTwoMsg);
		}
		}
		return res;
	}
	
	private SpeechletResponse evaluateVokabelQuiz(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case vokabeln: {
			res = askUserResponse(difficultyMsg);
			recState = RecognitionState.Vokabel; break;
		} case quiz: {
			res = askUserResponse(singleQuizMsg);
			recState = RecognitionState.SingleQuiz; break;
		} case menü: {
			res = askUserResponse(welcomeMsg);
			recState = RecognitionState.OneTwo; break;
		} default: {
			res = askUserResponse(errorVokabelQuizMsg);
		}
		}
		return res;
	}

	private SpeechletResponse evaluateVokabel(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case einfach: {
			selectQuestion();
			res = askUserResponse(question);
			recState = RecognitionState.AnswerVokabeln; break;
		} case mittel: {
			selectQuestion();
			res = askUserResponse(question);
			recState = RecognitionState.AnswerVokabeln; break;
		} case schwer: {
			selectQuestion();
			res = askUserResponse(question);
			recState = RecognitionState.AnswerVokabeln; break;
		} case menü: {
			res = askUserResponse(welcomeMsg);
			recState = RecognitionState.OneTwo; break;
		} default: {
			res = askUserResponse(errorVokabelMsg);
		}
		}
		return res;
	}
	
	/* sind sie bereit? */
	private SpeechletResponse evaluateSingleQuiz(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		
		
		case ja: {
			selectQuestion();
			res = askUserResponse(question);
			recState = RecognitionState.AnswerQuiz; break;
		} 
		
		case nein: {
			res = response(/*buildString(sumMsg, String.valueOf(sum), "")+" "+*/goodbyeMsg); break;
		}
		
		default: {
			res = askUserResponse(errorVokabelMsg);
		}
		}
		return res;
	}
	
	
	
	
	private SpeechletResponse evaluateWhichPlayer(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case playerone: {
			res = askUserResponse(SpielerEinsMsg);
			recState = RecognitionState.AnswerTwo; break;
		} case playertwo: {
			res = askUserResponse(SpielerZweiMsg);
			recState = RecognitionState.AnswerThree; break;
		} case menü: {
			res = askUserResponse(welcomeMsg);
			recState = RecognitionState.OneTwo; break;
		} default: {
			res = askUserResponse(errorSpielereinszweiMsg);
		}
		}
		return res;
	}
	
	private SpeechletResponse evaluateWhichPlayerThree(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case playerone: {
			res = askUserResponse(SpielerEinsMsg);
			recState = RecognitionState.AnswerFour; break;
		} case playertwo: {
			res = askUserResponse(SpielerZweiMsg);
			recState = RecognitionState.AnswerFive; break;
		} case menü: {
			res = askUserResponse(welcomeMsg);
			recState = RecognitionState.OneTwo; break;
		} default: {
			res = askUserResponse(errorSpielereinszweiMsg);
		}
		}
		return res;
	}

	
	/*in den Vokabeln*/
	private SpeechletResponse evaluateAnswerVokabeln(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		/*switch (ourUserIntent) {*/
		/*default :{*/
			/*if (ourUserIntent.equals(UserIntent.hello)
					|| ourUserIntent.equals(UserIntent.tree)
					|| ourUserIntent.equals(UserIntent.now)
					|| ourUserIntent.equals(UserIntent.maybe)
					|| ourUserIntent.equals(UserIntent.today)
					)*/ {
				logger.info("User answer ="+ ourUserIntent.name().toLowerCase()+ "/correct answer="+correctAnswer);
				if (ourUserIntent.name().toLowerCase().equals(correctAnswer)) {
					logger.info("User answer recognized as correct.");
					increaseSum();
					increaseQuestions();
					if (sum == 1000000) {
						res = response(correctMsg+" "+congratsMsg+" "+goodbyeMsg);
					} else {
						recState = RecognitionState.YesNoVokabeln;
						res = askUserResponse(correctMsg+" "+buildString(sumMsg, String.valueOf(sum), " ")+" "+continueMsg);
						/*recState = RecognitionState.YesNo;*/
					}
				} else {
					increaseQuestions();
					recState = RecognitionState.YesNoVokabeln;
					res = askUserResponse(wrongMsg+" "+buildString(sumMsg, String.valueOf(sum), " ")+" "+continueMsg);
				}
			} /*else {
				res = askUserResponse(errorAnswerMsg);
			}
		/*}
		}*/
		return res;
	}
	
	
	
	/*im Quiz*/
	private SpeechletResponse evaluateAnswerQuiz(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		/*switch (ourUserIntent) {*/
		/*default :{*/
			/*if (ourUserIntent.equals(UserIntent.hello)
					|| ourUserIntent.equals(UserIntent.tree)
					|| ourUserIntent.equals(UserIntent.now)
					|| ourUserIntent.equals(UserIntent.maybe)
					|| ourUserIntent.equals(UserIntent.today)
					)*/ {
				logger.info("User answer ="+ ourUserIntent.name().toLowerCase()+ "/correct answer="+correctAnswer);
				if (ourUserIntent.name().toLowerCase().equals(correctAnswer)) {
					logger.info("User answer recognized as correct.");
					increaseSum();
					increaseQuestions();
					if (sum == 1000000) {
						res = response(correctMsg+" "+congratsMsg+" "+goodbyeMsg);
					} else {
						recState = RecognitionState.YesNoQuiz;
						res = askUserResponse(correctMsg+" "+buildString(sumMsg, String.valueOf(sum), " ")+" "+continueMsg);
						/*recState = RecognitionState.YesNo;*/
					}
				} else {
					increaseQuestions();
					recState = RecognitionState.YesNoQuiz;
					res = askUserResponse(wrongMsg+" "+buildString(sumMsg, String.valueOf(sum), " ")+" "+continueMsg);
				}
			} /*else {
				res = askUserResponse(errorAnswerMsg);
			}
		/*}
		}*/
		return res;
	}
	
	private SpeechletResponse evaluateAnswerTwo(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		/*switch (ourUserIntent) {*/
		/*default :{*/
			/*if (ourUserIntent.equals(UserIntent.hello)
					|| ourUserIntent.equals(UserIntent.tree)
					|| ourUserIntent.equals(UserIntent.now)
					|| ourUserIntent.equals(UserIntent.maybe)
					|| ourUserIntent.equals(UserIntent.today)
					|| ourUserIntent.equals(UserIntent.bye)
					)*/ {
				logger.info("User answer ="+ ourUserIntent.name().toLowerCase()+ "/correct answer="+correctAnswer2);
				
				if (ourUserIntent.name().toLowerCase().equals(correctAnswer2)) {
					logger.info("User answer recognized as correct.");
					increaseSum2();
					increaseQuestions2();
					if (sum2 == 200) {
						recState = RecognitionState.YesNoLevel;
						res = askUserResponse(correctMsg+" "+buildString2(sumTwoMsg, String.valueOf(sum2), " ")+" "+buildString3(sumThreeMsg, String.valueOf(sum3), " ")+" "+playerOneWins+" "+continueLevelMsg);
					} else {
						recState = RecognitionState.YesNoTwo;
						res = askUserResponse(correctMsg+" "+buildString2(sumTwoMsg, String.valueOf(sum2), " ")+" "+buildString3(sumThreeMsg, String.valueOf(sum3), " ")+" "+continueMsg);
						/*recState = RecognitionState.YesNo;*/
					}
				} else {
					increaseQuestions2();
					recState = RecognitionState.YesNoTwo;
					res = askUserResponse(wrongMsg+" "+buildString2(sumTwoMsg, String.valueOf(sum2), " ")+" "+buildString3(sumThreeMsg, String.valueOf(sum3), " ")+" "+continueMsg);
				}
			} /*else {
				res = askUserResponse(errorAnswerMsg);
			}
		/*}
		}*/
				return res;
	}
	
	
	private SpeechletResponse evaluateAnswerThree(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		/*switch (ourUserIntent) {*/
		/*default :{*/
			/*if (ourUserIntent.equals(UserIntent.hello)
					|| ourUserIntent.equals(UserIntent.tree)
					|| ourUserIntent.equals(UserIntent.now)
					|| ourUserIntent.equals(UserIntent.maybe)
					|| ourUserIntent.equals(UserIntent.today)
					|| ourUserIntent.equals(UserIntent.bye)
					)*/ {
				logger.info("User answer ="+ ourUserIntent.name().toLowerCase()+ "/correct answer="+correctAnswer2);
				
				if (ourUserIntent.name().toLowerCase().equals(correctAnswer2)) {
					logger.info("User answer recognized as correct.");
					increaseSum3();
					increaseQuestions2();
					if (sum3 == 200) {
						/*res = response(correctMsg+" "+congratsMsg+" "+goodbyeMsg);*/
						recState = RecognitionState.YesNoLevel;
						res = askUserResponse(correctMsg+" "+buildString2(sumTwoMsg, String.valueOf(sum2), " ")+" "+buildString3(sumThreeMsg, String.valueOf(sum3), " ")+" "+playerTwoWins+" "+continueLevelMsg);
					} else {
						recState = RecognitionState.YesNoTwo;
						res = askUserResponse(correctMsg+" "+buildString2(sumTwoMsg, String.valueOf(sum2), " ")+" "+buildString3(sumThreeMsg, String.valueOf(sum3), " ")+" "+continueMsg);
						/*recState = RecognitionState.YesNo;*/
					}
				} else {
					increaseQuestions2();
					recState = RecognitionState.YesNoTwo;
					res = askUserResponse(wrongMsg+" "+buildString2(sumTwoMsg, String.valueOf(sum2), " ")+" "+buildString3(sumThreeMsg, String.valueOf(sum3), " ")+" "+continueMsg);
				}
			} /*else {
				res = askUserResponse(errorAnswerMsg);
			}
		/*}
		}*/
				return res;
	}
	
	
	private SpeechletResponse evaluateAnswerFour(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		/*switch (ourUserIntent) {*/
		/*default :{*/
			/*if (ourUserIntent.equals(UserIntent.hello)
					|| ourUserIntent.equals(UserIntent.tree)
					|| ourUserIntent.equals(UserIntent.now)
					|| ourUserIntent.equals(UserIntent.maybe)
					|| ourUserIntent.equals(UserIntent.today)
					|| ourUserIntent.equals(UserIntent.bye)
					)*/ {
				logger.info("User answer ="+ ourUserIntent.name().toLowerCase()+ "/correct answer="+correctAnswer3);
				if (ourUserIntent.name().toLowerCase().equals(correctAnswer3)) {
					logger.info("User answer recognized as correct.");
					increaseSum4();
					increaseQuestions3();
					if (sum4 == 200) {
						recState = RecognitionState.YesNoLevel;
						res = askUserResponse(correctMsg+" "+buildString2(sumTwoMsg, String.valueOf(sum2+sum4), " ")+" "+buildString3(sumThreeMsg, String.valueOf(sum3+sum5), " ")+" "+playerOneWins+" "+continueLevelMsg);
					} else {
						recState = RecognitionState.YesNoTwo;
						res = askUserResponse(correctMsg+" "+buildString2(sumTwoMsg, String.valueOf(sum2+sum4), " ")+" "+buildString3(sumThreeMsg, String.valueOf(sum3+sum5), " ")+" "+continueMsg);
						/*recState = RecognitionState.YesNo;*/
					}
				} else {
					increaseQuestions3();
					recState = RecognitionState.YesNoTwo;
					res = askUserResponse(wrongMsg+" "+buildString2(sumTwoMsg, String.valueOf(sum2+sum4), " ")+" "+buildString3(sumThreeMsg, String.valueOf(sum3+sum5), " ")+" "+continueMsg);
				}
			} /*else {
				res = askUserResponse(errorAnswerMsg);
			}
		/*}
		}*/
				return res;
	}
	

	
	private SpeechletResponse evaluateAnswerFive(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		/*switch (ourUserIntent) {*/
		/*default :{*/
			/*if (ourUserIntent.equals(UserIntent.hello)
					|| ourUserIntent.equals(UserIntent.tree)
					|| ourUserIntent.equals(UserIntent.now)
					|| ourUserIntent.equals(UserIntent.maybe)
					|| ourUserIntent.equals(UserIntent.today)
					|| ourUserIntent.equals(UserIntent.bye)
					)*/ {
				logger.info("User answer ="+ ourUserIntent.name().toLowerCase()+ "/correct answer="+correctAnswer3);
				if (ourUserIntent.name().toLowerCase().equals(correctAnswer3)) {
					logger.info("User answer recognized as correct.");
					increaseSum5();
					increaseQuestions3();
					if (sum5 == 200) {
						/*res = response(correctMsg+" "+congratsMsg+" "+goodbyeMsg);*/
						recState = RecognitionState.YesNoLevel;
						res = askUserResponse(correctMsg+" "+buildString2(sumTwoMsg, String.valueOf(sum2+sum4), " ")+" "+buildString3(sumThreeMsg, String.valueOf(sum3+sum5), " ")+" "+playerTwoWins+" "+continueLevelMsg);
					} else {
						recState = RecognitionState.YesNoTwo;
						res = askUserResponse(correctMsg+" "+buildString2(sumTwoMsg, String.valueOf(sum2+sum4), " ")+" "+buildString3(sumThreeMsg, String.valueOf(sum3+sum5), " ")+" "+continueMsg);
						/*recState = RecognitionState.YesNo;*/
					}
				} else {
					increaseQuestions3();
					recState = RecognitionState.YesNoTwo;
					res = askUserResponse(wrongMsg+" "+buildString2(sumTwoMsg, String.valueOf(sum2+sum4), " ")+" "+buildString3(sumThreeMsg, String.valueOf(sum3+sum5), " ")+" "+continueMsg);
				}
			} /*else {
				res = askUserResponse(errorAnswerMsg);
			}
		/*}
		}*/
				return res;
	}

	/*private void setfinalSum() {
		if (sum <500){
			sum = 0;
		}else{
			if(sum <16000){
				sum = 500;
			}else{
				sum=16000;
			}
		}

	}*/

	private void increaseSum() {
		switch(sum){
		case 0: sum = 50; break;
		case 50: sum = 100; break;
		case 100: sum = 200; break;
		case 200: sum = 300; break;
		case 300: sum = 500; break;
		case 500: sum = 1000; break;
		case 1000: sum = 2000; break;
		case 2000: sum = 4000; break;
		case 4000: sum = 8000; break;
		case 8000: sum = 16000; break;
		case 16000: sum = 32000; break;
		case 32000: sum = 64000; break;
		case 64000: sum = 125000; break;
		case 125000: sum = 500000; break;
		case 500000: sum = 1000000; break;
		}
	}
	
	private void increaseSum2() {
		switch(sum2){
		case 0: sum2 = 50; break;
		case 50: sum2 = 100; break;
		case 100: sum2 = 200; break;
		case 200: sum2 = 300; break;
		case 300: sum2 = 500; break;
		case 500: sum2 = 1000; break;
		case 1000: sum2 = 2000; break;
		case 2000: sum2 = 4000; break;
		case 4000: sum2 = 8000; break;
		case 8000: sum2 = 16000; break;
		case 16000: sum2 = 32000; break;
		case 32000: sum2 = 64000; break;
		case 64000: sum2 = 125000; break;
		case 125000: sum2 = 500000; break;
		case 500000: sum2 = 1000000; break;
		}
	}
	
	private void increaseSum3() {
		switch(sum3){
		case 0: sum3 = 50; break;
		case 50: sum3 = 100; break;
		case 100: sum3 = 200; break;
		case 200: sum3 = 300; break;
		case 300: sum3 = 500; break;
		case 500: sum3 = 1000; break;
		case 1000: sum3 = 2000; break;
		case 2000: sum3 = 4000; break;
		case 4000: sum3 = 8000; break;
		case 8000: sum3 = 16000; break;
		case 16000: sum3 = 32000; break;
		case 32000: sum3 = 64000; break;
		case 64000: sum3 = 125000; break;
		case 125000: sum3 = 500000; break;
		case 500000: sum3 = 1000000; break;
		}
	}
	
	private void increaseSum4() {
		switch(sum4){
		case 0: sum4 = 50; break;
		case 50: sum4 = 100; break;
		case 100: sum4 = 200; break;
		case 200: sum4 = 300; break;
		case 300: sum4 = 500; break;
		case 500: sum4 = 1000; break;
		case 1000: sum4 = 2000; break;
		case 2000: sum4 = 4000; break;
		case 4000: sum4 = 8000; break;
		case 8000: sum4 = 16000; break;
		case 16000: sum4 = 32000; break;
		case 32000: sum4 = 64000; break;
		case 64000: sum4 = 125000; break;
		case 125000: sum4 = 500000; break;
		case 500000: sum4 = 1000000; break;
		}
	}
	
	private void increaseSum5() {
		switch(sum5){
		case 0: sum5 = 50; break;
		case 50: sum5 = 100; break;
		case 100: sum5 = 200; break;
		case 200: sum5 = 300; break;
		case 300: sum5 = 500; break;
		case 500: sum5 = 1000; break;
		case 1000: sum5 = 2000; break;
		case 2000: sum5 = 4000; break;
		case 4000: sum5 = 8000; break;
		case 8000: sum5 = 16000; break;
		case 16000: sum5 = 32000; break;
		case 32000: sum5 = 64000; break;
		case 64000: sum5 = 125000; break;
		case 125000: sum5 = 500000; break;
		case 500000: sum5 = 1000000; break;
		}
	}
	
	private void increaseQuestions() {
		switch(questions){
		case 0: questions = 50; break;
		case 50: questions = 100; break;
		case 100: questions = 200; break;
		case 200: questions = 300; break;
		case 300: questions = 500; break;
		case 500: questions = 1000; break;
		case 1000: questions = 2000; break;
		case 2000: questions = 4000; break;
		case 4000: questions = 8000; break;
		case 8000: questions = 16000; break;
		case 16000: questions = 32000; break;
		case 32000: questions = 64000; break;
		case 64000: questions = 125000; break;
		case 125000: questions = 500000; break;
		case 500000: questions = 1000000; break;
		case 1000000: questions = 0; break;
		}
	}
	
	private void increaseQuestions2() {
		switch(questions2){
		case 0: questions2 = 50; break;
		case 50: questions2 = 100; break;
		case 100: questions2 = 200; break;
		case 200: questions2 = 300; break;
		case 300: questions2 = 500; break;
		case 500: questions2 = 1000; break;
		case 1000: questions2 = 2000; break;
		case 2000: questions2 = 4000; break;
		case 4000: questions2 = 8000; break;
		case 8000: questions2 = 16000; break;
		case 16000: questions2 = 32000; break;
		case 32000: questions2 = 64000; break;
		case 64000: questions2 = 125000; break;
		case 125000: questions2 = 500000; break;
		case 500000: questions2 = 1000000; break;
		case 1000000: questions = 0; break;
		}
	}
	
	private void increaseQuestions3() {
		switch(questions3){
		case 0: questions3 = 50; break;
		case 50: questions3 = 100; break;
		case 100: questions3 = 200; break;
		case 200: questions3 = 300; break;
		case 300: questions3 = 500; break;
		case 500: questions3 = 1000; break;
		case 1000: questions3 = 2000; break;
		case 2000: questions3 = 4000; break;
		case 4000: questions3 = 8000; break;
		case 8000: questions3 = 16000; break;
		case 16000: questions3 = 32000; break;
		case 32000: questions3 = 64000; break;
		case 64000: questions3 = 125000; break;
		case 125000: questions3 = 500000; break;
		case 500000: questions3 = 1000000; break;
		case 1000000: questions = 0; break;
		}
	}

	
	 void recognizeUserIntent(String userRequest) {
		userRequest = userRequest.toLowerCase();
		String pattern4 = "\\bnow\\b";
		String pattern5 = "\\btree\\b";
		String pattern6 = "\\bhello\\b";
		String pattern7 = "\\bweiter\\b";
		String pattern8 = "\\baufhören\\b";
		String pattern9 = "\\bmaybe\\b";
		String pattern10 = "\\btoday\\b";
		String pattern11 = "\\beiner\\b";
		String pattern12 = "\\bmehrere\\b";
		String pattern13 = "\\bvokabeln\\b";
		String pattern14 = "\\bquiz\\b";
		String pattern15 = "\\beinfach\\b";
		String pattern16 = "\\bmittel\\b";
		String pattern17 = "\\bschwer\\b";
		String pattern18 = "\\bplayerone\\b";
		String pattern19 = "\\bplayertwo\\b";
		String pattern20 = "\\bbye\\b";
		String pattern21 = "\\bmenü\\b";
		String pattern22 = "\\bmoin\\b";
		String pattern23 = "\\bnextlevel\\b";
		String pattern24 = "\\blight\\b";
		String pattern25 = "\\bone\\b";
		String pattern26 = "\\btwo\\b";
		String pattern27 = "\\bja\\b";
		String pattern28 = "\\bnein\\b";
		String pattern29 = "\\bquiz\\b";
		String pattern30 = "\\bbeenden\\b";
		
		

		Pattern p4 = Pattern.compile(pattern4);
		Matcher m4 = p4.matcher(userRequest);
		Pattern p5 = Pattern.compile(pattern5);
		Matcher m5 = p5.matcher(userRequest);
		Pattern p6 = Pattern.compile(pattern6);
		Matcher m6 = p6.matcher(userRequest);
		Pattern p7 = Pattern.compile(pattern7);
		Matcher m7 = p7.matcher(userRequest);
		Pattern p8 = Pattern.compile(pattern8);
		Matcher m8 = p8.matcher(userRequest);
		Pattern p9 = Pattern.compile(pattern9);
		Matcher m9 = p9.matcher(userRequest);
		Pattern p10 = Pattern.compile(pattern10);
		Matcher m10= p10.matcher(userRequest);
		Pattern p11 = Pattern.compile(pattern11);
		Matcher m11= p11.matcher(userRequest);
		Pattern p12 = Pattern.compile(pattern12);
		Matcher m12= p12.matcher(userRequest);
		Pattern p13 = Pattern.compile(pattern13);
		Matcher m13= p13.matcher(userRequest);
		Pattern p14 = Pattern.compile(pattern14);
		Matcher m14= p14.matcher(userRequest);
		Pattern p15 = Pattern.compile(pattern15);
		Matcher m15= p15.matcher(userRequest);
		Pattern p16 = Pattern.compile(pattern16);
		Matcher m16= p16.matcher(userRequest);
		Pattern p17 = Pattern.compile(pattern17);
		Matcher m17= p17.matcher(userRequest);
		Pattern p18 = Pattern.compile(pattern18);
		Matcher m18= p18.matcher(userRequest);
		Pattern p19 = Pattern.compile(pattern19);
		Matcher m19= p19.matcher(userRequest);
		Pattern p20 = Pattern.compile(pattern20);
		Matcher m20= p20.matcher(userRequest);
		Pattern p21 = Pattern.compile(pattern21);
		Matcher m21= p21.matcher(userRequest);
		Pattern p22 = Pattern.compile(pattern22);
		Matcher m22= p22.matcher(userRequest);
		Pattern p23 = Pattern.compile(pattern23);
		Matcher m23= p23.matcher(userRequest);
		Pattern p24 = Pattern.compile(pattern24);
		Matcher m24= p24.matcher(userRequest);
		Pattern p25 = Pattern.compile(pattern25);
		Matcher m25= p25.matcher(userRequest);
		Pattern p26 = Pattern.compile(pattern26);
		Matcher m26= p26.matcher(userRequest);
		Pattern p27 = Pattern.compile(pattern27);
		Matcher m27= p27.matcher(userRequest);
		Pattern p28 = Pattern.compile(pattern28);
		Matcher m28= p28.matcher(userRequest);
		Pattern p29 = Pattern.compile(pattern29);
		Matcher m29= p29.matcher(userRequest);
		Pattern p30 = Pattern.compile(pattern30);
		Matcher m30= p30.matcher(userRequest);
		
		if (m4.find()) {
			ourUserIntent = UserIntent.now;
		} else if (m5.find()) {
			ourUserIntent = UserIntent.tree;
		} else if (m6.find()) {
			ourUserIntent = UserIntent.hello;
		} else if (m7.find()) {
			ourUserIntent = UserIntent.weiter;
		} else if (m8.find()) {
			ourUserIntent = UserIntent.aufhören;
		} else if (m9.find()) {
			ourUserIntent = UserIntent.maybe;
		} else if (m10.find()) {
			ourUserIntent = UserIntent.today;
		} else if (m11.find()) {
			ourUserIntent = UserIntent.einer;
		} else if (m12.find()) {
			ourUserIntent = UserIntent.mehrere;
		} else if (m13.find()) {
			ourUserIntent = UserIntent.vokabeln;
		} else if (m14.find()) {
			ourUserIntent = UserIntent.quiz;
		} else if (m15.find()) {
			ourUserIntent = UserIntent.einfach;
		} else if (m16.find()) {
			ourUserIntent = UserIntent.mittel;
		} else if (m17.find()) {
			ourUserIntent = UserIntent.schwer;
		} else if (m18.find()) {
			ourUserIntent = UserIntent.playerone;
		} else if (m19.find()) {
			ourUserIntent = UserIntent.playertwo;
		} else if (m20.find()) {
			ourUserIntent = UserIntent.bye;
		} else if (m21.find()) {
			ourUserIntent = UserIntent.menü;
		} else if (m22.find()) {
			ourUserIntent = UserIntent.moin;
		} else if (m23.find()) {
			ourUserIntent = UserIntent.nextlevel;
		} else if (m24.find()) {
			ourUserIntent = UserIntent.light;
		} else if (m25.find()) {
			ourUserIntent = UserIntent.one;
		} else if (m26.find()) {
			ourUserIntent = UserIntent.two;
		} else if (m27.find()) {
			ourUserIntent = UserIntent.ja;
		} else if (m28.find()) {
			ourUserIntent = UserIntent.nein;
		} else if (m29.find()) {
			ourUserIntent = UserIntent.quiz;
		} else if (m30.find()) {
			ourUserIntent = UserIntent.beenden;
		} else {
			ourUserIntent = UserIntent.Error;
		}
		logger.info("set ourUserIntent to " +ourUserIntent);
	}

	//TODO
	/*private void useFiftyFiftyJoker() {
		answerOption1 = correctAnswer;
		answerOption2 = correctAnswer;
	}

	//TODO
	private void usePublikumJoker() {
		answerOption1 = correctAnswer;
	}*/

	/**
	 * formats the text in weird ways
	 * @param text
	 * @param i
	 * @return
	 */
	private SpeechletResponse responseWithFlavour(String text, int i) {

		SsmlOutputSpeech speech = new SsmlOutputSpeech();
		switch(i){ 
		case 0: 
			speech.setSsml("<speak><amazon:effect name=\"whispered\">" + text + "</amazon:effect></speak>");
			break; 
		case 1: 
			speech.setSsml("<speak><emphasis level=\"strong\">" + text + "</emphasis></speak>");
			break; 
		case 2: 
			String half1=text.split(" ")[0];
			String[] rest = Arrays.copyOfRange(text.split(" "), 1, text.split(" ").length);
			speech.setSsml("<speak>"+half1+"<break time=\"3s\"/>"+ StringUtils.join(rest," ") + "</speak>");
			break; 
		case 3: 
			String firstNoun="erstes Wort buchstabiert";
			String firstN=text.split(" ")[3];
			speech.setSsml("<speak>"+firstNoun+ "<say-as interpret-as=\"spell-out\">"+firstN+"</say-as>"+"</speak>");
			break; 
		case 4: 
			speech.setSsml("<speak><audio src='soundbank://soundlibrary/transportation/amzn_sfx_airplane_takeoff_whoosh_01'/></speak>");
			break;
		default: 
			speech.setSsml("<speak><amazon:effect name=\"whispered\">" + text + "</amazon:effect></speak>");
		} 

		return SpeechletResponse.newTellResponse(speech);
	}


	@Override
	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope)
	{
		logger.info("Alexa session ends now");
	}



	/**
	 * Tell the user something - the Alexa session ends after a 'tell'
	 */
	private SpeechletResponse response(String text)
	{
		// Create the plain text output.
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(text);

		return SpeechletResponse.newTellResponse(speech);
	}

	/**
	 * A response to the original input - the session stays alive after an ask request was send.
	 *  have a look on https://developer.amazon.com/de/docs/custom-skills/speech-synthesis-markup-language-ssml-reference.html
	 * @param text
	 * @return
	 */
	private SpeechletResponse askUserResponse(String text)
	{
		SsmlOutputSpeech speech = new SsmlOutputSpeech();
		speech.setSsml("<speak>" + text + "</speak>");

		// reprompt after 8 seconds
		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
		repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> Bist du noch da?</speak>");

		Reprompt rep = new Reprompt();
		rep.setOutputSpeech(repromptSpeech);

		return SpeechletResponse.newAskResponse(speech, rep);
	}


}
