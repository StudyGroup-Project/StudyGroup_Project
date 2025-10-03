package com.study.focus.notification.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNotification is a Querydsl query type for Notification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotification extends EntityPathBase<Notification> {

    private static final long serialVersionUID = -750831142L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNotification notification = new QNotification("notification");

    public final com.study.focus.common.domain.QBaseCreatedEntity _super = new com.study.focus.common.domain.QBaseCreatedEntity(this);

    public final com.study.focus.study.domain.QStudyMember actor;

    public final EnumPath<AudienceType> audienceType = createEnum("audienceType", AudienceType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.study.focus.study.domain.QStudy study;

    public final StringPath title = createString("title");

    public QNotification(String variable) {
        this(Notification.class, forVariable(variable), INITS);
    }

    public QNotification(Path<? extends Notification> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNotification(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNotification(PathMetadata metadata, PathInits inits) {
        this(Notification.class, metadata, inits);
    }

    public QNotification(Class<? extends Notification> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.actor = inits.isInitialized("actor") ? new com.study.focus.study.domain.QStudyMember(forProperty("actor"), inits.get("actor")) : null;
        this.study = inits.isInitialized("study") ? new com.study.focus.study.domain.QStudy(forProperty("study")) : null;
    }

}

