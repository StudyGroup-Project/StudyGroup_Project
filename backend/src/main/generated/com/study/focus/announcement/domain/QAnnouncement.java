package com.study.focus.announcement.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAnnouncement is a Querydsl query type for Announcement
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAnnouncement extends EntityPathBase<Announcement> {

    private static final long serialVersionUID = -1318519662L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAnnouncement announcement = new QAnnouncement("announcement");

    public final com.study.focus.common.domain.QBaseTimeEntity _super = new com.study.focus.common.domain.QBaseTimeEntity(this);

    public final com.study.focus.study.domain.QStudyMember author;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.study.focus.study.domain.QStudy study;

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAnnouncement(String variable) {
        this(Announcement.class, forVariable(variable), INITS);
    }

    public QAnnouncement(Path<? extends Announcement> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAnnouncement(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAnnouncement(PathMetadata metadata, PathInits inits) {
        this(Announcement.class, metadata, inits);
    }

    public QAnnouncement(Class<? extends Announcement> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.author = inits.isInitialized("author") ? new com.study.focus.study.domain.QStudyMember(forProperty("author"), inits.get("author")) : null;
        this.study = inits.isInitialized("study") ? new com.study.focus.study.domain.QStudy(forProperty("study")) : null;
    }

}

