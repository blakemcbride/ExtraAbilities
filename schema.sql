
create database extra_abilities;
\c extra_abilities

create table account (
    user_id VARCHAR(255) not null primary key,
    aid char(36) not null unique,   -- uuid
    when_created timestamp not null,
    last_seen timestamp not null,

    email VARCHAR(40),
    email_verified CHAR(1) NOT NULL DEFAULT 'N',
    email_verification_code CHAR(6) NOT NULL DEFAULT '',
    email_nverif smallint not null default 0,  -- number of times verification attempted

    timezone VARCHAR(30),

    constraint account_verified_chk check (email_verified = 'Y' or email_verified = 'N')
);

create table person (
    person_id varchar(255) not null primary key,
    pid char(36) not null unique,
    aid char(36) not null references account(aid),
    name varchar(60)
);

create index person_user_idx on person (aid);


create table list_name (
    listid char(36) primary key,
    aid char(36) not null references account(aid),
    pid char(36) references person(pid),
    name varchar(60) not null,  -- list name
    password varchar(60),
    unique (aid, pid, name)
);

create index list_name_pid_idx on list_name (pid);

create table list_detail (
    listid char(36) not null references list_name(listid),
    seq smallint not null,
    item varchar(4096),
    primary key (listid, seq) deferrable initially deferred
);

create table journal_name (
    jid char(36) primary key,
    aid char(36) not null references account(aid),
    pid char(36) references person(pid),
    name varchar(60) not null,  -- journal name
    password varchar(60),
    unique (aid, pid, name)
);

create index journal_name_pid_idx on journal_name (pid);

create table journal_detail (
    jid char(36) not null references journal_name(jid),
    jtimestamp timestamp with time zone not null,
    log text not null,
    primary key (jid, jtimestamp)
);
