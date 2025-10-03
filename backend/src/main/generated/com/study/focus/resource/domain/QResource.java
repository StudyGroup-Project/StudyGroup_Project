package com.study.focus.resource.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QResource is a Querydsl query type for Resource
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResource extends EntityPathBase<Resource> {

    private static final long serialVersionUID = 1162464672L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QResource resource = new QResource("resource");

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

    public QResource(String variable) {
        this(Resource.class, forVariable(variable), INITS);
    }

    public QResource(Path<? extends Resource> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QResource(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QResource(PathMetadata metadata, PathInits inits) {
        this(Resource.class, metadata, inits);
    }

    public QResource(Class<? extends Resource> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.author = inits.isInitialized("author") ? new com.study.focus.study.domain.QStudyMember(forProperty("author"), inits.get("author")) : null;
        this.study = inits.isInitialized("study") ? new com.study.focus.study.domain.QStudy(forProperty("study")) : null;
    }

}

