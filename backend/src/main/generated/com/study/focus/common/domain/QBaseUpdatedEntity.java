package com.study.focus.common.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBaseUpdatedEntity is a Querydsl query type for BaseUpdatedEntity
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QBaseUpdatedEntity extends EntityPathBase<BaseUpdatedEntity> {

    private static final long serialVersionUID = 1676024478L;

    public static final QBaseUpdatedEntity baseUpdatedEntity = new QBaseUpdatedEntity("baseUpdatedEntity");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QBaseUpdatedEntity(String variable) {
        super(BaseUpdatedEntity.class, forVariable(variable));
    }

    public QBaseUpdatedEntity(Path<? extends BaseUpdatedEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBaseUpdatedEntity(PathMetadata metadata) {
        super(BaseUpdatedEntity.class, metadata);
    }

}

