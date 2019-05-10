package com.upgrad.quora.api.controller;
import com.upgrad.quora.api.model.AnswerRequest;
import com.upgrad.quora.api.model.AnswerEditRequest;
import com.upgrad.quora.api.model.AnswerEditResponse;
import com.upgrad.quora.api.model.AnswerResponse;
import com.upgrad.quora.api.model.AnswerDeleteResponse;
import com.upgrad.quora.api.model.AnswerDetailsResponse;

import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.QuestionAnswerService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class AnswerController {

    @Autowired
    private QuestionAnswerService questionAnswerService;

    @Autowired
    private AuthenticationService authenticationService;

    //Create answer
    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(@RequestHeader("authorization") final String authorization, final AnswerRequest answerRequest,@PathVariable("questionId") final String questionUuid)
            throws AuthorizationFailedException, InvalidQuestionException {


        final AnswerEntity answerEntity = new AnswerEntity();

        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setAns(answerRequest.getAnswer());

        final AnswerEntity createdAnswerEntity = questionAnswerService.createAnswer(answerEntity,authorization,questionUuid);

        AnswerResponse answerResponse = new AnswerResponse().id(createdAnswerEntity.getUuid()).status("ANSWER CREATED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    }

    //Edit answer
    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse> editAnswerContent(@PathVariable("answerId") final String answerUuid,
                                                                    @RequestHeader("authorization") final String authorization,final AnswerEditRequest answerEditRequest)
            throws AuthorizationFailedException, AnswerNotFoundException {



        final AnswerEntity answerEntity = questionAnswerService.editAnswer(answerUuid, authorization,answerEditRequest.getContent());


        AnswerEditResponse answerEditResponse = new AnswerEditResponse().id(answerEntity.getUuid()).status("ANSWER EDITED");
        return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
    }

    //Delete answer
    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> deleteAnswer(@PathVariable("answerId") final String answerUuid,
                                                                 @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException,AnswerNotFoundException {

        final AnswerEntity answerEntity = questionAnswerService.deleteAnswer(answerUuid, authorization);
        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse().id(answerEntity.getUuid()).status("ANSWER DELETED");
        return new ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse, HttpStatus.OK);
    }

    //Get all answers to a question
    @RequestMapping(method = RequestMethod.GET, path = "answer/all/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<AnswerDetailsResponse>> getAllAnswersToQuestion (@RequestHeader("authorization") final String authorization, @PathVariable("questionId") final String questionUuid)
            throws AuthorizationFailedException, InvalidQuestionException {

        final List answers = questionAnswerService.getAllAnswersByQuestion(authorization,questionUuid);

        List<AnswerDetailsResponse> aresponses = new ArrayList<AnswerDetailsResponse>();
        for( Object answer:answers){
            AnswerEntity a = (AnswerEntity) answer;
            AnswerDetailsResponse answerDetailsResponse = new AnswerDetailsResponse().id(a.getUuid()).answerContent(a.getAns()).questionContent(a.getQuestion().getContent());
            aresponses.add(answerDetailsResponse);
        }

        return new ResponseEntity<List<AnswerDetailsResponse>>(aresponses, HttpStatus.OK);
    }
}
