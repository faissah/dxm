# DbFileSystem
drop table jr_fsg_FSENTRY;
drop table jr_fs_default_FSENTRY;
drop table jr_fs_live_FSENTRY;
drop table jr_fsv_FSENTRY;

# PersistenceManager
drop table jr_default_BUNDLE;
drop table jr_default_REFS;
drop table jr_default_BINVAL;
drop table jr_default_NAMES;
drop table jr_live_BUNDLE;
drop table jr_live_REFS;
drop table jr_live_BINVAL;
drop table jr_live_NAMES;
drop table jr_v_BUNDLE;
drop table jr_v_REFS;
drop table jr_v_BINVAL;
drop table jr_v_NAMES;

# Journal
drop table jr_j_JOURNAL;
drop table jr_j_GLOBAL_REVISION;
drop table jr_j_LOCAL_REVISIONS;

# DbFileSystem - global
create table jr_fsg_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA bytea null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index jr_fsg_FSENTRY_IDX on jr_fsg_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
# DbFileSystem - default workspace
create table jr_fs_default_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA bytea null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index jr_fs_default_FSENTRY_IDX on jr_fs_default_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
# DbFileSystem - live workspace
create table jr_fs_live_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA bytea null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index jr_fs_live_FSENTRY_IDX on jr_fs_live_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
# DbFileSystem - versioning
create table jr_fsv_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA bytea null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index jr_fsv_FSENTRY_IDX on jr_fsv_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);

# PersistenceManager - default workspace
create table jr_default_BUNDLE (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, BUNDLE_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table jr_default_REFS (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, REFS_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table jr_default_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA bytea not null);
create unique index jr_default_BINVAL_IDX on jr_default_BINVAL (BINVAL_ID);
create table jr_default_NAMES (ID SERIAL PRIMARY KEY, NAME varchar(255) not null);
# PersistenceManager - live workspace
create table jr_live_BUNDLE (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, BUNDLE_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table jr_live_REFS (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, REFS_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table jr_live_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA bytea not null);
create unique index jr_live_BINVAL_IDX on jr_live_BINVAL (BINVAL_ID);
create table jr_live_NAMES (ID SERIAL PRIMARY KEY, NAME varchar(255) not null);
# PersistenceManager - versioning
create table jr_v_BUNDLE (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, BUNDLE_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table jr_v_REFS (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, REFS_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table jr_v_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA bytea not null);
create unique index jr_v_BINVAL_IDX on jr_v_BINVAL (BINVAL_ID);
create table jr_v_NAMES (ID SERIAL PRIMARY KEY, NAME varchar(255) not null);

# Journal
create table jr_j_JOURNAL (REVISION_ID BIGINT NOT NULL, JOURNAL_ID varchar(255), PRODUCER_ID varchar(255), REVISION_DATA bytea);
create unique index jr_j_JOURNAL_IDX on jr_j_JOURNAL (REVISION_ID);
create table jr_j_GLOBAL_REVISION (REVISION_ID BIGINT NOT NULL);
create unique index jr_j_GLOBAL_REVISION_IDX on jr_j_GLOBAL_REVISION (REVISION_ID);
create table jr_j_LOCAL_REVISIONS (JOURNAL_ID varchar(255) NOT NULL, REVISION_ID BIGINT NOT NULL);
# Inserting the one and only revision counter record now helps avoiding race conditions
insert into jr_j_GLOBAL_REVISION VALUES(0);
