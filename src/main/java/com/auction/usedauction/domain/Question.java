package com.auction.usedauction.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Question extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    private String content;
    private int layer;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Question parent;

    @OneToMany(mappedBy = "parent")
    private List<Question> children = new ArrayList<>();

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Builder
    public Question(String content,int layer, Member member, Product product, Question parent) {
        this.content=content;
        this.product=product;
        this.member = member;
        this.layer = layer;
        changeParent(parent);
    }

    public void addChild(Question question) {
        question.changeParent(this);
    }

    public void changeParent(Question question) {
        if (question != null) {
            this.parent=question;
            question.getChildren().add(this);
        }

    }
}
