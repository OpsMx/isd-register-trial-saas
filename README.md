# isd-register
isd-register

Isd-Saas trail automation git repo link
    [https://github.com/OpsMx/trial-saas-3.12](url)    
    [https://github.com/OpsMx/saas-trial](url)


Exec into postgres pod and run the following cmds

CREATE TABLE users(id bigint NOT NULL,business_email   character varying(30),company_name  character varying(19),contact_number  character varying(12),first_name  character varying(18),last_name  character varying(17),created_at timestamp without time zone,updated_at timestamp without time zone); 

CREATE SEQUENCE hibernate_sequence START 1;	
