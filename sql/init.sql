use tutu;
CREATE TABLE `user` (
        `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
        `user_account` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
        `user_password` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
        `union_id` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信开放平台id',
        `mp_open_id` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公众号openId',
        `user_name` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户昵称',
        `user_avatar` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户头像',
        `user_profile` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户简介',
        `user_role` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin/ban',
        `edit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
        PRIMARY KEY (`id`),
        KEY `idx_union_id` (`union_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户';


create table if not exists picture (
    id bigint auto_increment comment  'id' primary key ,
    url varchar(512) not null comment '图片地址',
    name varchar(128) not null comment '图片名称',
    introduction varchar(512) null comment '图片简介',
    category varchar(128) null comment '图片分类',
    tags varchar(512) null comment '图片标签(json数组)',
    pic_size bigint comment '图片大小',
    pic_width int null comment '图片宽度',
    pic_height int null comment '图片高度',
    pic_scale double null comment '图片宽高比例',
    pic_format varchar(32) null comment '图片格式',
    user_id bigint not null comment '上传用户id',
    create_time datetime not null default current_timestamp comment '创建时间',
    edit_time datetime not null default current_timestamp comment '编辑时间',
    update_time datetime not null default current_timestamp on update current_timestamp comment '更新时间',
    is_delete tinyint not null default 0 comment  '是否删除',
    INDEX idx_name (name),
    INDEX idx_introduction (introduction),
    INDEX idx_category (category),
    INDEX idx_tags (tags),
    INDEX idx_user_id (user_id)
) comment '图片' collate 'utf8mb4_general_ci';