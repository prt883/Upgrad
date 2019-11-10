package com.upgrad.quora.api.controller;


import com.upgrad.quora.api.model.AnswerDeleteResponse;
import com.upgrad.quora.api.model.AnswerEditRequest;
import com.upgrad.quora.api.model.AnswerRequest;
import com.upgrad.quora.api.model.AnswerResponse;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AnswerController {
    @Autowired
    AnswerBusinessService answerBusinessService;

    @RequestMapping(value="question/{questionId}/answer/create",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(@PathVariable("questionId") String questionId, final AnswerRequest answerRequest, @RequestHeader("authorization") final String authorization) throws InvalidQuestionException, AuthorizationFailedException {

        String accessToken = authorization.startsWith("Bearer ")?authorization.split("Bearer ")[1]:authorization;
        String uuid=answerBusinessService.postAnswer(accessToken,questionId,answerRequest.getAnswer());

       final AnswerResponse answerResponse= new AnswerResponse();
       answerResponse.id(uuid).status("ANSWER CREATED");

       return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "answer/edit/{answerId}",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> editByAnswerId(@PathVariable("answerId") String answerId, final AnswerEditRequest answerEditRequest, @RequestHeader("authorization") final String authorization) throws AnswerNotFoundException, AuthorizationFailedException {

        String accessToken = authorization.startsWith("Bearer ")?authorization.split("Bearer ")[1]:authorization;
        String uuid=answerBusinessService.updateAnswer(answerId,answerEditRequest.getContent(),accessToken);

        AnswerResponse answerResponse=new AnswerResponse().id(uuid).status("ANSWER EDITED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> deleteAnswerByAnswerId(@PathVariable("answerId") String answerId, @RequestHeader("authorization") final String authorization) throws AnswerNotFoundException, AuthorizationFailedException {

        String accessToken = authorization.startsWith("Bearer ")?authorization.split("Bearer ")[1]:authorization;
        String uuid=answerBusinessService.deleteAnswer(answerId,accessToken);

        AnswerDeleteResponse answerDeleteResponse=new AnswerDeleteResponse().id(uuid).status("ANSWER DELETED");
        return new ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse, HttpStatus.OK);
    }

}
