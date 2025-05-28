CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

BEGIN;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'game_room_status') THEN
        CREATE TYPE game_room_status AS ENUM (
            'WAITING_FOR_PLAYERS',
            'IN_PROGRESS',
            'FINISHED',
            'ABANDONED'
        );
    END IF;
END$$;

-- #############################################################################
-- ### USERS AND USER_ROLES ###
-- #############################################################################

WITH inserted_users AS (
    INSERT INTO users (id, username, email, password, created_at) VALUES
    (uuid_generate_v4(), 'john_doe', 'john.doe@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '30 days'),
    (uuid_generate_v4(), 'jane_smith', 'jane.smith@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '25 days'),
    (uuid_generate_v4(), 'mike_brown', 'mike.brown@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '20 days'),
    (uuid_generate_v4(), 'sara_wilson', 'sara.wilson@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '15 days'),
    (uuid_generate_v4(), 'gamer_x', 'gamer.x@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '10 days'),
    (uuid_generate_v4(), 'mafia_boss', 'boss@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '5 days'),
    (uuid_generate_v4(), 'detective_sam', 'sam@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '4 days'),
    (uuid_generate_v4(), 'civ_anna', 'anna@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '3 days'),
    (uuid_generate_v4(), 'pro_player_1', 'pro1@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '2 days'),
    (uuid_generate_v4(), 'newbie_player', 'newbie@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '1 day'),
    -- Admini
    (uuid_generate_v4(), 'admin_one', 'admin1@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '50 days'),
    (uuid_generate_v4(), 'admin_two', 'admin2@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '45 days'),
    -- Dodatkowi użytkownicy dla większej ilości danych
    (uuid_generate_v4(), 'player_alpha', 'alpha@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '100 days'),
    (uuid_generate_v4(), 'player_beta', 'beta@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '90 days'),
    (uuid_generate_v4(), 'player_gamma', 'gamma@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '80 days'),
    (uuid_generate_v4(), 'player_delta', 'delta@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '70 days'),
    (uuid_generate_v4(), 'player_epsilon', 'epsilon@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '60 days'),
    (uuid_generate_v4(), 'player_zeta', 'zeta@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '55 days'),
    (uuid_generate_v4(), 'player_eta', 'eta@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '40 days'),
    (uuid_generate_v4(), 'player_theta', 'theta@example.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF.GHIJKLMNOPQRSTUVWXYZ012345', NOW() - interval '35 days')
    RETURNING id, username
)
INSERT INTO user_roles (user_id, role_name)
SELECT id, 'ROLE_USER' FROM inserted_users;

INSERT INTO user_roles (user_id, role_name)
SELECT id, 'ROLE_ADMIN' FROM users WHERE username IN ('admin_one', 'admin_two', 'mafia_boss');


-- #############################################################################
-- ### (GAME_ROOMS) ###
-- #############################################################################
WITH hosts_cte AS (
    SELECT id FROM users WHERE username IN ('john_doe', 'jane_smith', 'mike_brown', 'sara_wilson', 'gamer_x', 'mafia_boss', 'admin_one', 'player_alpha', 'player_beta', 'player_gamma', 'player_delta', 'player_epsilon', 'player_zeta', 'player_eta', 'player_theta')
),
room_data AS (
    SELECT
        uuid_generate_v4() as id,
        substr(md5(random()::text), 1, 8) as room_code, -- Prosty sposób na "unikalny" kod
        name,
        host_id,
        max_players,
        status_text,
        created_at
    FROM (
        VALUES
        ('Wieczorna Mafia', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 8, 'WAITING_FOR_PLAYERS', NOW() - interval '5 days'),
        ('Szybka Gra', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 6, 'IN_PROGRESS', NOW() - interval '4 days'),
        ('Weekendowi Wojownicy', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 10, 'FINISHED', NOW() - interval '10 days'),
        ('Sesja Strategiczna', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 5, 'WAITING_FOR_PLAYERS', NOW() - interval '1 day'),
        ('Opuszczone Lobby', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 7, 'ABANDONED', NOW() - interval '12 days'),
        ('Stara Gra Alfa', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 8, 'FINISHED', NOW() - interval '30 days'),
        ('Podejrzani', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 6, 'IN_PROGRESS', NOW() - interval '2 days'),
        ('Nocne Rozgrywki', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 9, 'WAITING_FOR_PLAYERS', NOW() - interval '6 hours'),
        ('Turniej Mistrzów', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 12, 'IN_PROGRESS', NOW() - interval '3 days'),
        ('Pokój dla Początkujących', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 4, 'WAITING_FOR_PLAYERS', NOW() - interval '2 hours'),
        ('Tajemnicze Morderstwo', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 7, 'FINISHED', NOW() - interval '15 days'),
        ('Mafia o Zmierzchu', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 8, 'IN_PROGRESS', NOW() - interval '1 day'),
        ('Klub Detektywów', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 5, 'WAITING_FOR_PLAYERS', NOW() - interval '7 days'),
        ('Ostateczna Konfrontacja', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 10, 'FINISHED', NOW() - interval '22 days'),
        ('Cicha Noc', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 6, 'ABANDONED', NOW() - interval '18 days'),
        ('Gra dla Weteranów', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 8, 'IN_PROGRESS', NOW() - interval '48 hours'),
        ('Niespodziewani Goście', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 7, 'WAITING_FOR_PLAYERS', NOW() - interval '12 hours'),
        ('Zakończona Intryga', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 9, 'FINISHED', NOW() - interval '40 days'),
        ('Arena Walk', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 10, 'IN_PROGRESS', NOW() - interval '60 hours'),
        ('Pusty Pokój Testowy', (SELECT id FROM hosts_cte ORDER BY random() LIMIT 1), 4, 'WAITING_FOR_PLAYERS', NOW() - interval '30 minutes')
    ) AS t(name, host_id, max_players, status_text, created_at)
)
INSERT INTO game_rooms (id, room_code, name, host_id, max_players, status, created_at)
SELECT id, room_code, name, host_id, max_players, status_text::game_room_status, created_at FROM room_data;


-- #############################################################################
-- ### PLAYERS_IN_ROOMS ###
-- #############################################################################

WITH room_cte AS (SELECT id, host_id, created_at FROM game_rooms WHERE name = 'Wieczorna Mafia' LIMIT 1)
INSERT INTO players_in_rooms (id, user_id, game_room_id, nickname_in_room, is_alive, joined_at)
SELECT uuid_generate_v4(), room_cte.host_id, room_cte.id, (SELECT username FROM users WHERE id = room_cte.host_id), TRUE, room_cte.created_at + interval '5 minutes' FROM room_cte;

WITH room_cte AS (SELECT id, created_at FROM game_rooms WHERE name = 'Wieczorna Mafia' LIMIT 1),
     player_user_cte AS (SELECT id, username FROM users WHERE username = 'jane_smith' LIMIT 1)
INSERT INTO players_in_rooms (id, user_id, game_room_id, nickname_in_room, is_alive, joined_at)
SELECT uuid_generate_v4(), player_user_cte.id, room_cte.id, player_user_cte.username, TRUE, room_cte.created_at + interval '10 minutes' FROM room_cte, player_user_cte;

WITH room_cte AS (SELECT id, host_id, created_at FROM game_rooms WHERE name = 'Szybka Gra' LIMIT 1)
INSERT INTO players_in_rooms (id, user_id, game_room_id, nickname_in_room, is_alive, joined_at)
SELECT uuid_generate_v4(), room_cte.host_id, room_cte.id, (SELECT username FROM users WHERE id = room_cte.host_id), TRUE, room_cte.created_at + interval '1 minute' FROM room_cte;

WITH room_cte AS (SELECT id, created_at FROM game_rooms WHERE name = 'Szybka Gra' LIMIT 1),
     player_user_cte AS (SELECT id, username FROM users WHERE username = 'john_doe' LIMIT 1)
INSERT INTO players_in_rooms (id, user_id, game_room_id, nickname_in_room, is_alive, joined_at)
SELECT uuid_generate_v4(), player_user_cte.id, room_cte.id, player_user_cte.username, FALSE, room_cte.created_at + interval '2 minutes' FROM room_cte, player_user_cte; -- Załóżmy, że zginął

WITH room_cte AS (SELECT id, created_at FROM game_rooms WHERE name = 'Szybka Gra' LIMIT 1),
     player_user_cte AS (SELECT id, username FROM users WHERE username = 'mike_brown' LIMIT 1)
INSERT INTO players_in_rooms (id, user_id, game_room_id, nickname_in_room, is_alive, joined_at)
SELECT uuid_generate_v4(), player_user_cte.id, room_cte.id, player_user_cte.username, TRUE, room_cte.created_at + interval '3 minutes' FROM room_cte, player_user_cte;

WITH room_cte AS (SELECT id, host_id, created_at FROM game_rooms WHERE name = 'Weekendowi Wojownicy' LIMIT 1)
INSERT INTO players_in_rooms (id, user_id, game_room_id, nickname_in_room, is_alive, joined_at)
SELECT uuid_generate_v4(), room_cte.host_id, room_cte.id, (SELECT username FROM users WHERE id = room_cte.host_id), FALSE, room_cte.created_at + interval '5 minutes' FROM room_cte;

WITH room_cte AS (SELECT id, created_at FROM game_rooms WHERE name = 'Weekendowi Wojownicy' LIMIT 1),
     player_user1_cte AS (SELECT id, username FROM users WHERE username = 'sara_wilson' LIMIT 1),
     player_user2_cte AS (SELECT id, username FROM users WHERE username = 'gamer_x' LIMIT 1)
INSERT INTO players_in_rooms (id, user_id, game_room_id, nickname_in_room, is_alive, joined_at) VALUES
(uuid_generate_v4(), (SELECT id FROM player_user1_cte), (SELECT id FROM room_cte), (SELECT username FROM player_user1_cte), FALSE, (SELECT created_at FROM room_cte) + interval '10 minutes'),
(uuid_generate_v4(), (SELECT id FROM player_user2_cte), (SELECT id FROM room_cte), (SELECT username FROM player_user2_cte), TRUE, (SELECT created_at FROM room_cte) + interval '15 minutes'); -- Załóżmy, że gamer_x wygrał

WITH room_cte AS (SELECT id, host_id, created_at FROM game_rooms WHERE name = 'Sesja Strategiczna' LIMIT 1)
INSERT INTO players_in_rooms (id, user_id, game_room_id, nickname_in_room, is_alive, joined_at)
SELECT uuid_generate_v4(), room_cte.host_id, room_cte.id, (SELECT username FROM users WHERE id = room_cte.host_id), TRUE, room_cte.created_at + interval '1 minute' FROM room_cte;

WITH room_cte AS (SELECT id, created_at FROM game_rooms WHERE name = 'Sesja Strategiczna' LIMIT 1),
     player_user_cte AS (SELECT id, username FROM users WHERE username = 'detective_sam' LIMIT 1)
INSERT INTO players_in_rooms (id, user_id, game_room_id, nickname_in_room, is_alive, joined_at)
SELECT uuid_generate_v4(), player_user_cte.id, room_cte.id, 'SlySam', TRUE, room_cte.created_at + interval '5 minutes' FROM room_cte, player_user_cte;

DO $$
DECLARE
    room_record RECORD;
    user_record RECORD;
    player_count INTEGER;
    i INTEGER;
    random_username_val TEXT;
    user_id_val UUID;
BEGIN
    FOR room_record IN SELECT gr.id, gr.host_id, gr.created_at, gr.max_players, gr.name FROM game_rooms gr LOOP
        IF NOT EXISTS (SELECT 1 FROM players_in_rooms pir WHERE pir.game_room_id = room_record.id AND pir.user_id = room_record.host_id) THEN
            INSERT INTO players_in_rooms (id, user_id, game_room_id, nickname_in_room, is_alive, joined_at)
            VALUES (uuid_generate_v4(), room_record.host_id, room_record.id, (SELECT u.username FROM users u WHERE u.id = room_record.host_id), TRUE, room_record.created_at + interval '1 minute');
        END IF;

        SELECT COUNT(*) INTO player_count FROM players_in_rooms pir WHERE pir.game_room_id = room_record.id;
        IF room_record.max_players > player_count THEN
            player_count := floor(random() * (room_record.max_players - player_count));
            IF player_count < 0 THEN player_count := 0; END IF;

            FOR i IN 1..player_count LOOP
                SELECT u.username, u.id INTO random_username_val, user_id_val
                FROM users u
                WHERE u.id != room_record.host_id
                  AND u.id NOT IN (SELECT pir.user_id FROM players_in_rooms pir WHERE pir.game_room_id = room_record.id)
                ORDER BY random() LIMIT 1;

                IF user_id_val IS NOT NULL THEN
                     INSERT INTO players_in_rooms (id, user_id, game_room_id, nickname_in_room, is_alive, joined_at)
                     VALUES (uuid_generate_v4(), user_id_val, room_record.id, random_username_val, CASE WHEN random() > 0.2 THEN TRUE ELSE FALSE END, room_record.created_at + (floor(random()*10)+2 || ' minutes')::interval);
                END IF;
            END LOOP;
        END IF;
    END LOOP;
END $$;

-- #############################################################################
-- ### REFRESH_TOKENS ###
-- #############################################################################

INSERT INTO refresh_tokens (id, token, user_id, expires_at, created_at, revoked, device_info) VALUES
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'john_doe'), NOW() + interval '7 days', NOW() - interval '1 hour', FALSE, 'Chrome on Windows'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'jane_smith'), NOW() + interval '6 days', NOW() - interval '2 hours', FALSE, 'Firefox on Linux'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'mafia_boss'), NOW() + interval '10 days', NOW() - interval '30 minutes', FALSE, 'Mobile App iOS'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'admin_one'), NOW() + interval '14 days', NOW() - interval '1 day', FALSE, 'Admin Panel Browser'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'gamer_x'), NOW() + interval '5 days', NOW() - interval '5 hours', FALSE, 'Gaming PC Client'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'player_alpha'), NOW() + interval '3 days', NOW() - interval '6 hours', FALSE, 'Tablet Android'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'player_beta'), NOW() + interval '8 days', NOW() - interval '12 hours', FALSE, 'Safari on macOS'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'player_gamma'), NOW() + interval '2 days', NOW() - interval '18 hours', FALSE, 'Edge on Windows'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'player_delta'), NOW() + interval '9 days', NOW() - interval '24 hours', FALSE, 'Opera on Linux'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'player_epsilon'), NOW() + interval '1 days', NOW() - interval '30 hours', FALSE, 'Brave Browser'),

(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'john_doe'), NOW() - interval '1 day', NOW() - interval '8 days', FALSE, 'Old Chrome Session'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'sara_wilson'), NOW() - interval '3 days', NOW() - interval '10 days', FALSE, 'Expired Mobile Token'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'mike_brown'), NOW() - interval '10 days', NOW() - interval '20 days', FALSE, 'Ancient Session'),

(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'jane_smith'), NOW() + interval '2 days', NOW() - interval '3 days', TRUE, 'Revoked Firefox Session'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'admin_two'), NOW() - interval '5 days', NOW() - interval '15 days', TRUE, 'Revoked Admin Old Device'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'player_zeta'), NOW() + interval '1 day', NOW() - interval '2 days', TRUE, 'User Requested Revoke'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'player_eta'), NOW() - interval '1 day', NOW() - interval '3 days', TRUE, 'Security Alert Revoke'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'player_theta'), NOW() + interval '4 days', NOW() - interval '1 day', TRUE, 'Device Change Revoke'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'newbie_player'), NOW() + interval '6 days', NOW() - interval '12 hours', FALSE, 'Fresh Token Newbie'),
(uuid_generate_v4(), substr(md5(random()::text), 1, 32), (SELECT id FROM users WHERE username = 'pro_player_1'), NOW() + interval '7 days', NOW() - interval '6 hours', FALSE, 'Pro Player Active Session');


COMMIT;