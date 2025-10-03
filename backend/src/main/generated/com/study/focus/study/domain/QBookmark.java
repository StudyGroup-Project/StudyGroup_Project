package com.study.focus.study.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBookmark is a Querydsl query type for Bookmark
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookmark extends EntityPathBase<Bookmark> {

    private static final long serialVersionUID = -891088717L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBookmark bookmark = new QBookmark("bookmark");

    public final com.study.focus.common.domain.QBaseCreatedEntity _super = new com.study.focus.common.domain.QBaseCreatedEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QStudy study;

    public final com.study.focus.account.domain.QUser user;

    public QBookmark(String variable) {
        this(Bookmark.class, forVariable(variable), INITS);
    }

    public QBookmark(Path<? extends Bookmark> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBookmark(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBookmark(PathMetadata metadata, PathInits inits) {
        this(Bookmark.class, metadata, inits);
    }

    public QBookmark(Class<? extends Bookmark> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.study = inits.isInitialized("study") ? new QStudy(forProperty("study")) : null;
        this.user = inits.isInitialized("user") ? new com.study.focus.account.domain.QUser(forProperty("user")) : null;
    }

}

