package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class AuthenticationService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;


    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity authenticate(final String username, final String password) throws AuthenticationFailedException {
        UserEntity userEntity = userDao.getUserByUsername(username);
        if (userEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "This username does not exist");
        }

        final String encryptedPassword = cryptographyProvider.encrypt(password, userEntity.getSalt());
        if (encryptedPassword.equals(userEntity.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            UserAuthTokenEntity userAuthToken = new UserAuthTokenEntity();
            userAuthToken.setUuid(userEntity.getUuid());
            userAuthToken.setUser(userEntity);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            userAuthToken.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(), now, expiresAt));
            userAuthToken.setLoginAt(now);
            userAuthToken.setExpiresAt(expiresAt);
            userDao.createAuthToken(userAuthToken);

            userDao.updateUser(userEntity);


            return userAuthToken;
        } else {
            throw new AuthenticationFailedException("ATH-002", "Password Failed");
        }

    }
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity logout(String accessToken) throws SignOutRestrictedException, AuthorizationFailedException {

        UserAuthTokenEntity userAuthTokenEntity=userDao.getUserAuthToken(accessToken);
        if(userAuthTokenEntity==null){
            throw new SignOutRestrictedException("SGR-001","User is not Signed in");
        }
        if(userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())){
            throw new AuthorizationFailedException("ATHR-004","Token Expired.");
        }
        userAuthTokenEntity.setLogoutAt(ZonedDateTime.now());
        userDao.updateUserAuthTokenEntity(userAuthTokenEntity);
        return userAuthTokenEntity;
    }



}
