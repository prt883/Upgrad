package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class QuestionBusinessService {
    @Autowired
    UserDao userDao;

    @Autowired
    QuestionDao questionDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public String postQuestion(String accessToken,String content) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity=userDao.getUserAuthToken(accessToken);
        if(userAuthTokenEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        if(userAuthTokenEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post a question");
        }
        if(userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())){
            throw new AuthorizationFailedException("ATHR-004","Token Expired. Sign in again");
        }


        final QuestionEntity questionEntity=new QuestionEntity();
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setContent(content);
        questionEntity.setUser(userAuthTokenEntity.getUser());
        final ZonedDateTime now = ZonedDateTime.now();
        questionEntity.setDate(now);

        String uuid=questionDao.createQuestion(questionEntity).getUuid();
        return uuid;
    }

    public List<QuestionEntity> getAllQuestions(String accessToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity=userDao.getUserAuthToken(accessToken);
        if(userAuthTokenEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        if(userAuthTokenEntity.getLogoutAt()!=null){
          throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get all questions");
        }
        if(userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())){
            throw new AuthorizationFailedException("ATHR-004","Token Expired. Sign in again");
        }
        return questionDao.getAllQuestions();
    }


    public List<QuestionEntity> getAllQuestionsByUserId(String userId, String accessToken) throws UserNotFoundException, AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity=userDao.getUserAuthToken(accessToken);

        if(userAuthTokenEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        if(userAuthTokenEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get all questions posted by a specific user");
        }
        if(userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())){
            throw new AuthorizationFailedException("ATHR-004","Token Expired. Sign in again");
        }
        UserEntity userEntity=userDao.getUser(userId);

        if(userEntity==null){
            throw new UserNotFoundException("USR-001","User with entered uuid whose question details are to be seen does not exist");
        }
        return userEntity.getQuestionEntityList();
    }



    @Transactional(propagation = Propagation.REQUIRED)
    public String updateQuestion(String questionId,String content,String accessToken) throws InvalidQuestionException, AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity=userDao.getUserAuthToken(accessToken);

        if(userAuthTokenEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        if(userAuthTokenEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to edit the question");
        }
        if(userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())){
            throw new AuthorizationFailedException("ATHR-004","Token Expired. Sign in again");
        }
        QuestionEntity questionEntity=questionDao.getQuestionByUuid(questionId);
        if(questionEntity==null){
            throw new InvalidQuestionException("QUES-001","Entered question uuid does not exist");
        }
        if(userAuthTokenEntity.getUser().getId()==questionEntity.getUser().getId()){
            questionEntity.setContent(content);
            final ZonedDateTime now = ZonedDateTime.now();
            questionEntity.setDate(now);
            questionDao.updateQuestion(questionEntity);

            return questionId;
        }
        throw new AuthorizationFailedException("ATHR-003","Only the question owner can edit the question");
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteQuestion(String questionId,String accessToken) throws InvalidQuestionException, AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity=userDao.getUserAuthToken(accessToken);

        if(userAuthTokenEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        if(userAuthTokenEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to delete a question");
        }
        if(userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())){
            throw new AuthorizationFailedException("ATHR-004","Token Expired. Sign in again");
        }
        QuestionEntity questionEntity=questionDao.getQuestionByUuid(questionId);
        if(questionEntity==null){
            throw new InvalidQuestionException("QUES-001","Entered question uuid does not exist");
        }
        if(userAuthTokenEntity.getUser().getId()==questionEntity.getUser().getId() || userAuthTokenEntity.getUser().getRole().equals("admin")){
            return questionDao.deleteQuestionByUuid(questionId);

        }
        throw new AuthorizationFailedException("ATHR-003","Only the question owner or admin can delete the question");
    }

}


