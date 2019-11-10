package com.upgrad.quora.service.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "answer")
@NamedQueries(
        {
                @NamedQuery(name = "answerByUuid", query = "select a from AnswerEntity a where a.uuid = :uuid")
        }
)


public class AnswerEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "UUID")
    @Size(max = 200)
    @NotNull
    private String uuid;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "USER_ID")
    @NotNull
    private UserEntity user;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "QUESTION_ID")
    @NotNull
    private QuestionEntity questionEntity;

    @Column(name = "ans")
    @NotNull
    @Size(max = 255)
    private String ans;

    @Column(name = "DATE")
    @NotNull
    private ZonedDateTime date;


    public Integer getId() {return id; }

    public void setId(Integer id) {this.id = id; }

    public String getUuid() {return uuid; }

    public void setUuid(String uuid) {this.uuid = uuid; }

    public UserEntity getUser() {return user; }

    public void setUser(UserEntity user) { this.user = user; }

    public QuestionEntity getQuestionEntity() { return questionEntity; }

    public void setQuestionEntity(QuestionEntity questionEntity) { this.questionEntity = questionEntity; }

    public String getAns() {return ans; }

    public void setAns(String ans) {this.ans = ans; }

    public ZonedDateTime getDate() {return date; }

    public void setDate(ZonedDateTime date) {this.date = date; }

    @Override
    public boolean equals(Object obj) {
        return new EqualsBuilder().append(this, obj).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this).hashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }


}