package com.study.focus.study.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStudyMember is a Querydsl query type for StudyMember
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStudyMember extends EntityPathBase<StudyMember> {

    private static final long serialVersionUID = -790965978L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStudyMember studyMember = new QStudyMember("studyMember");

    public final com.study.focus.common.domain.QBaseCreatedEntity _super = new com.study.focus.common.domain.QBaseCreatedEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> exitedAt = createDateTime("exitedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<StudyRole> role = createEnum("role", StudyRole.class);

    public final EnumPath<StudyMemberStatus> status = createEnum("status", StudyMemberStatus.class);

    public final QStudy study;

    public final com.study.focus.account.domain.QUser user;

    public QStudyMember(String variable) {
        this(StudyMember.class, forVariable(variable), INITS);
    }

    public QStudyMember(Path<? extends StudyMember> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStudyMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStudyMember(PathMetadata metadata, PathInits inits) {
        this(StudyMember.class, metadata, inits);
    }

    public QStudyMember(Class<? extends StudyMember> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.study = inits.isInitialized("study") ? new QStudy(forProperty("study")) : null;
        this.user = inits.isInitialized("user") ? new com.study.focus.account.domain.QUser(forProperty("user")) : null;
    }

}

