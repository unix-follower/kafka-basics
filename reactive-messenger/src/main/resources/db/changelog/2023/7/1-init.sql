CREATE TABLE app_user (
  user_id UUID NOT NULL PRIMARY KEY,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  first_name VARCHAR(50),
  last_name VARCHAR(50),
  email VARCHAR(255),
  CONSTRAINT un_app_user UNIQUE (email)
);

INSERT INTO app_user (user_id, created_at, updated_at, first_name, last_name, email) VALUES
('ef5ef8d3-082c-4459-ada7-f9f207674c3f', now(), now(), 'Arstem', 'Nikitsenka', 'test@example.com');

CREATE TABLE channel (
  channel_id UUID NOT NULL PRIMARY KEY,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255)
);

INSERT INTO channel (channel_id, created_at, updated_at, name, description) VALUES
('8aabe24e-f1e9-4d6a-a795-e2d991b1002e', now(), now(), 'test', NULL);

CREATE TABLE channel_member (
  channel_member_id BIGSERIAL NOT NULL PRIMARY KEY,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  channel_id UUID NOT NULL,
  user_id UUID NOT NULL,
  CONSTRAINT fk_channel_member_user_id FOREIGN KEY (user_id)
   REFERENCES app_user(user_id)
   ON DELETE CASCADE
);
COMMENT ON TABLE channel_member is 'Association between channels and users';

INSERT INTO channel_member (channel_member_id, created_at, updated_at, channel_id, user_id) VALUES
(1, now(), now(), '8aabe24e-f1e9-4d6a-a795-e2d991b1002e', 'ef5ef8d3-082c-4459-ada7-f9f207674c3f');

CREATE TABLE message (
  message_id UUID NOT NULL PRIMARY KEY,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  data VARCHAR(1000) NOT NULL,
  user_id UUID,
  channel_id UUID NOT NULL,
  CONSTRAINT fk_channel_member_user_id FOREIGN KEY (user_id)
    REFERENCES app_user(user_id)
    ON DELETE SET NULL,
  CONSTRAINT fk_message_channel_id FOREIGN KEY (channel_id)
   REFERENCES channel(channel_id)
   ON DELETE CASCADE
);
