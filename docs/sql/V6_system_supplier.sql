ALTER TABLE `system_supplier`
    ADD COLUMN `company_name`     VARCHAR(100)  DEFAULT NULL COMMENT '公司名称'       AFTER `intro`,
    ADD COLUMN `company_address`  VARCHAR(255)  DEFAULT NULL COMMENT '公司地址'       AFTER `company_name`,
    ADD COLUMN `contact1_name`    VARCHAR(50)   DEFAULT NULL COMMENT '联系人1姓名'    AFTER `company_address`,
    ADD COLUMN `contact1_title`   VARCHAR(50)   DEFAULT NULL COMMENT '联系人1职务'    AFTER `contact1_name`,
    ADD COLUMN `contact1_phone`   VARCHAR(20)   DEFAULT NULL COMMENT '联系人1电话'    AFTER `contact1_title`,
    ADD COLUMN `contact2_name`    VARCHAR(50)   DEFAULT NULL COMMENT '联系人2姓名'    AFTER `contact1_phone`,
    ADD COLUMN `contact2_title`   VARCHAR(50)   DEFAULT NULL COMMENT '联系人2职务'    AFTER `contact2_name`,
    ADD COLUMN `contact2_phone`   VARCHAR(20)   DEFAULT NULL COMMENT '联系人2电话'    AFTER `contact2_title`,
    ADD COLUMN `business_license` VARCHAR(500)  DEFAULT NULL COMMENT '营业执照'       AFTER `contact2_phone`,
    ADD COLUMN `tourism_license`  VARCHAR(500)  DEFAULT NULL COMMENT '旅游业务许可证' AFTER `business_license`,
    ADD COLUMN `id_card_front`    VARCHAR(500)  DEFAULT NULL COMMENT '法人身份证正面' AFTER `tourism_license`,
    ADD COLUMN `id_card_back`     VARCHAR(500)  DEFAULT NULL COMMENT '法人身份证反面' AFTER `id_card_front`,
    ADD COLUMN `insurance_image`  VARCHAR(500)  DEFAULT NULL COMMENT '保险证明'       AFTER `id_card_back`;
