package com.upgrad.quora.api.controller;
import com.upgrad.quora.api.model.QuestionRequest;
import com.upgrad.quora.api.model.QuestionEditRequest;
import com.upgrad.quora.api.model.QuestionResponse;
import com.upgrad.quora.api.model.QuestionEditResponse;
import com.upgrad.quora.api.model.QuestionDetailsResponse;


import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.QuestionAnswerService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
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
public class QuestionController {
    @Autowired
    private QuestionAnswerService questionAnswerService;

    @Autowired
    private AuthenticationService authenticationService;

    @RequestMapping(method = RequestMethod.POST, path = "/question/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(final QuestionRequest questionRequest,@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {


        final QuestionEntity questionEntity = new QuestionEntity();

        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setContent(questionRequest.getContent());

        final QuestionEntity createdQuestionEntity = questionAnswerService.createQuestion(questionEntity,authorization);

        QuestionResponse questionResponse = new QuestionResponse().id(createdQuestionEntity.getUuid()).status("QUESTION CREATED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/question/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {

        final List questions = questionAnswerService.getAllQuestions(authorization);
        List<QuestionDetailsResponse> qresponses = new ArrayList<QuestionDetailsResponse>();
        for( Object question:questions){
            QuestionEntity q = (QuestionEntity)question;
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse().id(q.getUuid()).content(q.getContent());
            qresponses.add(questionDetailsResponse);
        }

        return new ResponseEntity<List<QuestionDetailsResponse>>(qresponses, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestionContent(@PathVariable("questionId") final String userUuid,
                                                       @RequestHeader("authorization") final String authorization,final QuestionEditRequest questionEditRequest) throws AuthorizationFailedException, InvalidQuestionException {



        final QuestionEntity questionEntity = questionAnswerService.editQuestion(userUuid, authorization,questionEditRequest.getContent());


        QuestionEditResponse questionEditResponse = new QuestionEditResponse().id(questionEntity.getUuid()).status("QUESTION EDITED");
        return new ResponseEntity<QuestionEditResponse>(questionEditResponse, HttpStatus.OK);
    }


}
