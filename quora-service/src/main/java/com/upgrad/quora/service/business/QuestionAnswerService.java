package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Service
public class QuestionAnswerService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private QuestionDao questionDao;

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
}
