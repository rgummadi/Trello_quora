package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;


    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity createUser(final UserEntity userEntity) throws SignUpRestrictedException {

        // check if user already exists
        String userName = userEntity.getUserName();
        UserEntity userEntity1 = userDao.getUserByUserName(userName);

        if (userEntity1 != null) {
            //return exception
            throw new SignUpRestrictedException("SGR-001","Try any other Username, this Username has already been taken");

        }

        String email = userEntity.getEmail();
        UserEntity userEntity2 = userDao.getUserByEmail(email);
        if(userEntity2 != null) {
            //return exception
            throw new SignUpRestrictedException("SGR-002","This user has already been registered, try with any other emailId");
        }

        String password = userEntity.getPassword();
        if (password == null) {
            userEntity.setPassword("quora@123");
        }
        String[] encryptedText = cryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);
        return userDao.createUser(userEntity);

    }
}


