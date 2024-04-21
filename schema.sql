--
-- PostgreSQL database dump
--

-- Dumped from database version 12.18 (Ubuntu 12.18-0ubuntu0.20.04.1)
-- Dumped by pg_dump version 12.18 (Ubuntu 12.18-0ubuntu0.20.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: account; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.account (
    user_id character varying(255) NOT NULL,
    aid character(36) NOT NULL,
    when_created timestamp without time zone NOT NULL,
    last_seen timestamp without time zone NOT NULL,
    email character varying(40),
    email_verified character(1) DEFAULT 'N'::bpchar NOT NULL,
    email_verification_code character(6) DEFAULT ''::bpchar NOT NULL,
    email_nverif smallint DEFAULT 0 NOT NULL,
    timezone character varying(30),
    CONSTRAINT account_verified_chk CHECK (((email_verified = 'Y'::bpchar) OR (email_verified = 'N'::bpchar)))
);


ALTER TABLE public.account OWNER TO postgres;

--
-- Name: journal_detail; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.journal_detail (
    jid character(36) NOT NULL,
    jdate integer NOT NULL,
    jseconds integer NOT NULL,
    log text NOT NULL
);


ALTER TABLE public.journal_detail OWNER TO postgres;

--
-- Name: journal_name; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.journal_name (
    jid character(36) NOT NULL,
    aid character(36) NOT NULL,
    name character varying(60) NOT NULL,
    password character varying(60),
    pid character(36)
);


ALTER TABLE public.journal_name OWNER TO postgres;

--
-- Name: list_detail; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.list_detail (
    listid character(36) NOT NULL,
    seq smallint NOT NULL,
    item character varying(4096)
);


ALTER TABLE public.list_detail OWNER TO postgres;

--
-- Name: list_name; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.list_name (
    listid character(36) NOT NULL,
    aid character(36) NOT NULL,
    pid character(36),
    name character varying(60) NOT NULL,
    password character varying(60)
);


ALTER TABLE public.list_name OWNER TO postgres;

--
-- Name: person; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.person (
    person_id character varying(255) NOT NULL,
    pid character(36) NOT NULL,
    aid character(36) NOT NULL,
    name character varying(60)
);


ALTER TABLE public.person OWNER TO postgres;

--
-- Name: account account_aid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.account
    ADD CONSTRAINT account_aid_key UNIQUE (aid);


--
-- Name: account account_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.account
    ADD CONSTRAINT account_pkey PRIMARY KEY (user_id);


--
-- Name: journal_detail journal_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal_detail
    ADD CONSTRAINT journal_detail_pkey PRIMARY KEY (jid, jdate, jseconds);


--
-- Name: journal_name journal_name_pidx; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal_name
    ADD CONSTRAINT journal_name_pidx UNIQUE (aid, pid, name);


--
-- Name: journal_name journal_name_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal_name
    ADD CONSTRAINT journal_name_pkey PRIMARY KEY (jid);


--
-- Name: list_detail list_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.list_detail
    ADD CONSTRAINT list_detail_pkey PRIMARY KEY (listid, seq) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: list_name list_name_aid_pid_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.list_name
    ADD CONSTRAINT list_name_aid_pid_name_key UNIQUE (aid, pid, name);


--
-- Name: list_name list_name_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.list_name
    ADD CONSTRAINT list_name_pkey PRIMARY KEY (listid);


--
-- Name: person person_pid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_pid_key UNIQUE (pid);


--
-- Name: person person_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_pkey PRIMARY KEY (person_id);


--
-- Name: journal_name_pid_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX journal_name_pid_idx ON public.journal_name USING btree (pid);


--
-- Name: list_name_pid_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX list_name_pid_idx ON public.list_name USING btree (pid);


--
-- Name: person_user_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX person_user_idx ON public.person USING btree (aid);


--
-- Name: journal_detail journal_detail_jid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal_detail
    ADD CONSTRAINT journal_detail_jid_fkey FOREIGN KEY (jid) REFERENCES public.journal_name(jid);


--
-- Name: journal_name journal_name_aid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal_name
    ADD CONSTRAINT journal_name_aid_fkey FOREIGN KEY (aid) REFERENCES public.account(aid);


--
-- Name: journal_name journal_name_pid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal_name
    ADD CONSTRAINT journal_name_pid_fkey FOREIGN KEY (pid) REFERENCES public.person(pid);


--
-- Name: list_detail list_detail_listid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.list_detail
    ADD CONSTRAINT list_detail_listid_fkey FOREIGN KEY (listid) REFERENCES public.list_name(listid);


--
-- Name: list_name list_name_aid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.list_name
    ADD CONSTRAINT list_name_aid_fkey FOREIGN KEY (aid) REFERENCES public.account(aid);


--
-- Name: list_name list_name_pid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.list_name
    ADD CONSTRAINT list_name_pid_fkey FOREIGN KEY (pid) REFERENCES public.person(pid);


--
-- Name: person person_aid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_aid_fkey FOREIGN KEY (aid) REFERENCES public.account(aid);


--
-- PostgreSQL database dump complete
--

