package com.study.focus.assignment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSubmission is a Querydsl query type for Submission
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSubmission extends EntityPathBase<Submission> {

    private static final long serialVersionUID = 2136809309L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSubmission submission = new QSubmission("submission");

    public final com.study.focus.common.domain.QBaseCreatedEntity _super = new com.study.focus.common.domain.QBaseCreatedEntity(this);

    public final QAssignment assignment;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.study.focus.study.domain.QStudyMember submitter;

    public QSubmission(String variable) {
        this(Submission.class, forVariable(variable), INITS);
    }

    public QSubmission(Path<? extends Submission> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSubmission(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSubmission(PathMetadata metadata, PathInits inits) {
        this(Submission.class, metadata, inits);
    }

    public QSubmission(Class<? extends Submission> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.assignment = inits.isInitialized("assignment") ? new QAssignment(forProperty("assignment"), inits.get("assignment")) : null;
        this.submitter = inits.isInitialized("submitter") ? new com.study.focus.study.domain.QStudyMember(forProperty("submitter"), inits.get("submitter")) : null;
    }

}

