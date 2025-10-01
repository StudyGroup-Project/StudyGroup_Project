package com.study.focus.account.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSystemCredential is a Querydsl query type for SystemCredential
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSystemCredential extends EntityPathBase<SystemCredential> {

    private static final long serialVersionUID = 724906183L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSystemCredential systemCredential = new QSystemCredential("systemCredential");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath loginId = createString("loginId");

    public final StringPath password = createString("password");

    public final QUser user;

    public QSystemCredential(String variable) {
        this(SystemCredential.class, forVariable(variable), INITS);
    }

    public QSystemCredential(Path<? extends SystemCredential> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSystemCredential(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSystemCredential(PathMetadata metadata, PathInits inits) {
        this(SystemCredential.class, metadata, inits);
    }

    public QSystemCredential(Class<? extends SystemCredential> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

