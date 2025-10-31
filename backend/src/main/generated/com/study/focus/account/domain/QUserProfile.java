package com.study.focus.account.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserProfile is a Querydsl query type for UserProfile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserProfile extends EntityPathBase<UserProfile> {

    private static final long serialVersionUID = 1222900189L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserProfile userProfile = new QUserProfile("userProfile");

    public final com.study.focus.common.domain.QBaseUpdatedEntity _super = new com.study.focus.common.domain.QBaseUpdatedEntity(this);

    public final com.study.focus.common.domain.QAddress address;

    public final DatePath<java.time.LocalDate> birthDate = createDate("birthDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<Job> job = createEnum("job", Job.class);

    public final StringPath nickname = createString("nickname");

    public final ListPath<com.study.focus.common.domain.Category, EnumPath<com.study.focus.common.domain.Category>> preferredCategory = this.<com.study.focus.common.domain.Category, EnumPath<com.study.focus.common.domain.Category>>createList("preferredCategory", com.study.focus.common.domain.Category.class, EnumPath.class, PathInits.DIRECT2);

    public final com.study.focus.common.domain.QFile profileImage;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QUserProfile(String variable) {
        this(UserProfile.class, forVariable(variable), INITS);
    }

    public QUserProfile(Path<? extends UserProfile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserProfile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserProfile(PathMetadata metadata, PathInits inits) {
        this(UserProfile.class, metadata, inits);
    }

    public QUserProfile(Class<? extends UserProfile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.address = inits.isInitialized("address") ? new com.study.focus.common.domain.QAddress(forProperty("address")) : null;
        this.profileImage = inits.isInitialized("profileImage") ? new com.study.focus.common.domain.QFile(forProperty("profileImage"), inits.get("profileImage")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

