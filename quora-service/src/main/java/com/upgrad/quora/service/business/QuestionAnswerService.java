package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class QuestionAnswerService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private QuestionDao questionDao;

    //Create Question
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity createQuestion(QuestionEntity questionEntity, final String authorizationToken)
            throws AuthorizationFailedException {


        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthEntity == null) {
            throw  new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        ZonedDateTime logoutTime = userAuthEntity.getLogoutAt();
        if (logoutTime != null) {
            throw  new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post a question");
        }

        questionEntity.setUser(userAuthEntity.getUser());
        questionEntity.setDate(LocalDateTime.now());

        questionDao.createQuestion(questionEntity);
        return questionEntity;
    }

    //Get All Questions
    @Transactional(propagation = Propagation.REQUIRED)
    public List<QuestionEntity> getAllQuestions(final String authToken) throws AuthorizationFailedException {

        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authToken);
        if (userAuthEntity == null) {
            throw  new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        ZonedDateTime logoutTime = userAuthEntity.getLogoutAt();
        if (logoutTime != null) {
            throw  new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get all questions");
        }

        List questions = questionDao.getAllQuestions();


        return questions;


    }

    //Edit question
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity editQuestion(final String questionUuid, final String authToken, final String content) throws AuthorizationFailedException,InvalidQuestionException {

        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authToken);
        if (userAuthEntity == null) {
            throw  new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        ZonedDateTime logoutTime = userAuthEntity.getLogoutAt();
        if (logoutTime != null) {
            throw  new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to edit the question");
        }

        UserEntity userEntity = userAuthEntity.getUser();

        QuestionEntity questionEntity = questionDao.getQuestionByUuid(questionUuid);


        if(questionEntity == null){
            throw new InvalidQuestionException("QUES-001","Entered question uuid does not exist");
        }

        if(userEntity.getId() != questionEntity.getUser().getId()) {
            throw  new AuthorizationFailedException("ATHR-003","Only the question owner can edit the question");
        }

        //update question in database
        questionEntity.setContent(content);
        questionDao.updateQuestion(questionEntity);

        return questionEntity;
    }


    //Delete Question
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity deleteQuestion(final String questionUuid, final String authToken) throws InvalidQuestionException,
            AuthorizationFailedException {


        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authToken);
        if (userAuthEntity == null) {
            throw  new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        ZonedDateTime logoutTime = userAuthEntity.getLogoutAt();
        if (logoutTime != null) {
            throw  new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to delete a question");
        }

        QuestionEntity questionEntity = questionDao.getQuestionByUuid(questionUuid);
        if ( questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }

        if(userAuthEntity.getUser().getRole() == "nonadmin" && userAuthEntity.getUser().getId() != questionEntity.getUser().getId()){
            throw  new AuthorizationFailedException("ATHR-003","Only the question owner or admin can delete the question");
        }

        questionDao.deleteQuestion(questionEntity);
        return questionEntity;
    }

    //Get All Questions By User
    @Transactional(propagation = Propagation.REQUIRED)
    public List<QuestionEntity> getAllQuestionsByUser(final String authToken, final String userUuid) throws AuthorizationFailedException, UserNotFoundException {

        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authToken);
        if (userAuthEntity == null) {
            throw  new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        ZonedDateTime logoutTime = userAuthEntity.getLogoutAt();
        if (logoutTime != null) {
            throw  new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get all questions posted by a specific user");
        }

        //Get user from uuid
        UserEntity userEntity = userDao.getUserByUuid(userUuid);
        if (userEntity == null) {
            throw  new UserNotFoundException("ATHR-003","User with entered uuid whose question details are to be seen does not exist");
        }

        List questions = questionDao.getAllQuestionsByUser(userEntity);


        return questions;


    }

    //Create Answer
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity createAnswer(AnswerEntity answerEntity, final String authToken, final String questionUuid)
            throws AuthorizationFailedException,InvalidQuestionException {

        QuestionEntity questionEntity = questionDao.getQuestionByUuid(questionUuid);
        if (questionEntity == null) {
            throw  new InvalidQuestionException("QUES-001","The question entered is invalid");
        }

        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authToken);
        if (userAuthEntity == null) {
            throw  new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        ZonedDateTime logoutTime = userAuthEntity.getLogoutAt();
        if (logoutTime != null) {
            throw  new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post an answer");
        }

        answerEntity.setUser(userAuthEntity.getUser());
        answerEntity.setDate(LocalDateTime.now());
        answerEntity.setQuestion(questionEntity);

        questionDao.createAnswer(answerEntity);
        return answerEntity;
    }

    //Edit answer
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity editAnswer(final String answerUuid, final String authToken, final String answer) throws AuthorizationFailedException,
            AnswerNotFoundException {

        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authToken);
        if (userAuthEntity == null) {
            throw  new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        ZonedDateTime logoutTime = userAuthEntity.getLogoutAt();
        if (logoutTime != null) {
            throw  new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to edit an answer");
        }

        UserEntity userEntity = userAuthEntity.getUser();

        AnswerEntity answerEntity = questionDao.getAnswerByUuid(answerUuid);


        if(answerEntity == null){
            throw new AnswerNotFoundException("ANS-001","Entered answer uuid does not exist");
        }

        if(userEntity.getId() != answerEntity.getUser().getId()) {
            throw  new AuthorizationFailedException("ATHR-003","Only the answer owner can edit the question");
        }

        //update question in database
        answerEntity.setAns(answer);
        questionDao.updateAnswer(answerEntity);

        return answerEntity;
    }

    //Delete Answer
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(final String answerUuid, final String authToken) throws AnswerNotFoundException,
            AuthorizationFailedException {


        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authToken);
        if (userAuthEntity == null) {
            throw  new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        ZonedDateTime logoutTime = userAuthEntity.getLogoutAt();
        if (logoutTime != null) {
            throw  new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to delete an answer");
        }

        AnswerEntity answerEntity = questionDao.getAnswerByUuid(answerUuid);
        if ( answerEntity == null) {
            throw new AnswerNotFoundException("QUES-001", "Entered answer uuid does not exist");
        }

        if(userAuthEntity.getUser().getRole() == "nonadmin" && userAuthEntity.getUser().getId() != answerEntity.getUser().getId()){
            throw  new AuthorizationFailedException("ATHR-003","Only the answer owner or admin can delete the answer");
        }

        questionDao.deleteAnswer(answerEntity);
        return answerEntity;
    }

    //Get All answers By Question
    @Transactional(propagation = Propagation.REQUIRED)
    public List<AnswerEntity> getAllAnswersByQuestion(final String authToken, final String questionUuid)
            throws AuthorizationFailedException, InvalidQuestionException {

        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authToken);
        if (userAuthEntity == null) {
            throw  new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        ZonedDateTime logoutTime = userAuthEntity.getLogoutAt();
        if (logoutTime != null) {
            throw  new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get the answers");
        }

        //Get user from uuid
       QuestionEntity questionEntity = questionDao.getQuestionByUuid(questionUuid);
        if (questionEntity == null) {
            throw  new InvalidQuestionException("QUES-001","The question with entered uuid whose details are to be seen does not exist");
        }

        List answers = questionDao.getAllAnswersByQuestion(questionEntity);


        return answers;


    }
}
