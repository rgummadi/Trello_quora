package com.upgrad.quora.api.controller;
import com.upgrad.quora.api.model.QuestionRequest;
import com.upgrad.quora.api.model.QuestionResponse;

import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.QuestionAnswerService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping
public class QuestionController {
    @Autowired
    private QuestionAnswerService questionAnswerService;

    @Autowired
    private AuthenticationService authenticationService;

    @RequestMapping(method = RequestMethod.POST, path = "/question/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQ(final QuestionRequest questionRequest,@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {


        final QuestionEntity questionEntity = new QuestionEntity();

        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setContent(questionRequest.getContent());

        final QuestionEntity createdQuestionEntity = questionAnswerService.createQuestion(questionEntity,authorization);
        QuestionResponse questionResponse = new QuestionResponse().id(createdQuestionEntity.getUuid()).status("QUESTION CREATED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }


}
