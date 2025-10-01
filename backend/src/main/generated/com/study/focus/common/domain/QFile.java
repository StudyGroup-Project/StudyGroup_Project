package com.study.focus.common.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFile is a Querydsl query type for File
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFile extends EntityPathBase<File> {

    private static final long serialVersionUID = -2045946773L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFile file = new QFile("file");

    public final QBaseCreatedEntity _super = new QBaseCreatedEntity(this);

    public final com.study.focus.announcement.domain.QAnnouncement announcement;

    public final com.study.focus.assignment.domain.QAssignment assignment;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath fileKey = createString("fileKey");

    public final StringPath fileName = createString("fileName");

    public final NumberPath<Long> fileSize = createNumber("fileSize", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final StringPath mimeType = createString("mimeType");

    public final com.study.focus.resource.domain.QResource resource;

    public final com.study.focus.assignment.domain.QSubmission submission;

    public QFile(String variable) {
        this(File.class, forVariable(variable), INITS);
    }

    public QFile(Path<? extends File> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFile(PathMetadata metadata, PathInits inits) {
        this(File.class, metadata, inits);
    }

    public QFile(Class<? extends File> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.announcement = inits.isInitialized("announcement") ? new com.study.focus.announcement.domain.QAnnouncement(forProperty("announcement"), inits.get("announcement")) : null;
        this.assignment = inits.isInitialized("assignment") ? new com.study.focus.assignment.domain.QAssignment(forProperty("assignment"), inits.get("assignment")) : null;
        this.resource = inits.isInitialized("resource") ? new com.study.focus.resource.domain.QResource(forProperty("resource"), inits.get("resource")) : null;
        this.submission = inits.isInitialized("submission") ? new com.study.focus.assignment.domain.QSubmission(forProperty("submission"), inits.get("submission")) : null;
    }

}

