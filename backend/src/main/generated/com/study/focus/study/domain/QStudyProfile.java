package com.study.focus.study.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStudyProfile is a Querydsl query type for StudyProfile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStudyProfile extends EntityPathBase<StudyProfile> {

    private static final long serialVersionUID = -8448867L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStudyProfile studyProfile = new QStudyProfile("studyProfile");

    public final com.study.focus.common.domain.QBaseUpdatedEntity _super = new com.study.focus.common.domain.QBaseUpdatedEntity(this);

    public final com.study.focus.common.domain.QAddress address;

    public final StringPath bio = createString("bio");

    public final EnumPath<com.study.focus.common.domain.Category> category = createEnum("category", com.study.focus.common.domain.Category.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QStudy study;

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QStudyProfile(String variable) {
        this(StudyProfile.class, forVariable(variable), INITS);
    }

    public QStudyProfile(Path<? extends StudyProfile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStudyProfile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStudyProfile(PathMetadata metadata, PathInits inits) {
        this(StudyProfile.class, metadata, inits);
    }

    public QStudyProfile(Class<? extends StudyProfile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.address = inits.isInitialized("address") ? new com.study.focus.common.domain.QAddress(forProperty("address")) : null;
        this.study = inits.isInitialized("study") ? new QStudy(forProperty("study")) : null;
    }

}

