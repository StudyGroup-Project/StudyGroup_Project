package com.study.focus.account.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOAuthCredential is a Querydsl query type for OAuthCredential
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOAuthCredential extends EntityPathBase<OAuthCredential> {

    private static final long serialVersionUID = -55786131L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOAuthCredential oAuthCredential = new QOAuthCredential("oAuthCredential");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<Provider> provider = createEnum("provider", Provider.class);

    public final StringPath providerUserId = createString("providerUserId");

    public final QUser user;

    public QOAuthCredential(String variable) {
        this(OAuthCredential.class, forVariable(variable), INITS);
    }

    public QOAuthCredential(Path<? extends OAuthCredential> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOAuthCredential(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOAuthCredential(PathMetadata metadata, PathInits inits) {
        this(OAuthCredential.class, metadata, inits);
    }

    public QOAuthCredential(Class<? extends OAuthCredential> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

