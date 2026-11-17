-- PostgreSQL 15+. Prerequisite owned by Person 2: public.app_users(id BIGINT PRIMARY KEY).
-- Shared proposed contract: public.app_users, a single conversations/members model.
-- No account table is created here. Apply after Person 2's account migration.
BEGIN;
CREATE TABLE friendships (
    user_low BIGINT NOT NULL REFERENCES app_users(id),
    user_high BIGINT NOT NULL REFERENCES app_users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(user_low,user_high), CHECK(user_low < user_high)
);
CREATE TABLE friend_requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES app_users(id),
    receiver_id BIGINT NOT NULL REFERENCES app_users(id),
    status VARCHAR(12) NOT NULL DEFAULT 'pending' CHECK(status IN ('pending','accepted','rejected','cancelled')),
    note TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMPTZ,
    CHECK(sender_id <> receiver_id),
    CHECK((status='pending' AND responded_at IS NULL) OR (status<>'pending' AND responded_at IS NOT NULL))
);
CREATE UNIQUE INDEX uq_pending_friend_pair ON friend_requests(LEAST(sender_id,receiver_id),GREATEST(sender_id,receiver_id)) WHERE status='pending';
CREATE INDEX ix_request_inbox ON friend_requests(receiver_id,status,created_at DESC);
CREATE TABLE user_blocks (
    blocker_id BIGINT NOT NULL REFERENCES app_users(id),
    blocked_id BIGINT NOT NULL REFERENCES app_users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(blocker_id,blocked_id), CHECK(blocker_id<>blocked_id)
);
CREATE TABLE conversations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    kind VARCHAR(10) NOT NULL CHECK(kind IN ('direct','group')),
    name VARCHAR(120),
    created_by BIGINT NOT NULL REFERENCES app_users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    encryption_mode VARCHAR(10) NOT NULL DEFAULT 'none' CHECK(encryption_mode IN ('none','e2ee')),
    direct_low BIGINT REFERENCES app_users(id),
    direct_high BIGINT REFERENCES app_users(id),
    CHECK((kind='direct' AND name IS NULL AND direct_low IS NOT NULL AND direct_high IS NOT NULL AND direct_low<direct_high)
       OR (kind='group' AND name IS NOT NULL AND LENGTH(TRIM(name))>0 AND direct_low IS NULL AND direct_high IS NULL)),
    CHECK(kind<>'direct' OR created_by IN (direct_low,direct_high)),
    UNIQUE(direct_low,direct_high)
);
CREATE TABLE conversation_members (
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_users(id),
    role VARCHAR(10) NOT NULL DEFAULT 'member' CHECK(role IN ('admin','member')),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMPTZ,
    history_hidden_before TIMESTAMPTZ,
    PRIMARY KEY(conversation_id,user_id),
    CHECK(left_at IS NULL OR left_at>=joined_at)
);
CREATE INDEX ix_member_user ON conversation_members(user_id,conversation_id);
CREATE TABLE messages (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL,
    body TEXT,
    ciphertext BYTEA,
    nonce BYTEA,
    key_version INTEGER,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(id,conversation_id),
    FOREIGN KEY(conversation_id,sender_id) REFERENCES conversation_members(conversation_id,user_id),
    CHECK((body IS NOT NULL AND LENGTH(TRIM(body))>0 AND ciphertext IS NULL AND nonce IS NULL AND key_version IS NULL)
       OR (body IS NULL AND ciphertext IS NOT NULL AND OCTET_LENGTH(ciphertext)>0 AND nonce IS NOT NULL AND OCTET_LENGTH(nonce)>0 AND key_version IS NOT NULL AND key_version>0))
);
CREATE INDEX ix_message_history ON messages(conversation_id,sent_at,id);
CREATE INDEX ix_message_sender ON messages(sender_id,sent_at);
CREATE TABLE message_user_states (
    message_id BIGINT NOT NULL,
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    hidden_at TIMESTAMPTZ,
    read_at TIMESTAMPTZ,
    PRIMARY KEY(message_id,user_id),
    FOREIGN KEY(message_id,conversation_id) REFERENCES messages(id,conversation_id) ON DELETE CASCADE,
    FOREIGN KEY(conversation_id,user_id) REFERENCES conversation_members(conversation_id,user_id)
);
-- Wrapped per-member conversation keys only. NEVER store plaintext private keys here.
CREATE TABLE conversation_member_keys (
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    key_version INTEGER NOT NULL CHECK(key_version>0),
    wrapped_key BYTEA NOT NULL CHECK(OCTET_LENGTH(wrapped_key)>0),
    algorithm VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(conversation_id,user_id,key_version),
    FOREIGN KEY(conversation_id,user_id) REFERENCES conversation_members(conversation_id,user_id)
);
-- Server operations in later versions must enforce authorization, active membership,
-- at least one active group admin, direct-member consistency, block rules,
-- request acceptance + friendship insertion in one transaction, and encryption mode.
-- They are intentionally not represented as working UI/backend features in version 1.
COMMIT;
