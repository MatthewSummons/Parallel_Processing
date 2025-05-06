-- User Generation & Access Grant
CREATE USER 'u3035946760'@'localhost' IDENTIFIED BY 'C3358PASS';
GRANT ALL ON TwentyFour.* TO 'u3035946760'@'localhost';

CREATE TABLE UserInfo(
    -- UserID     INT         NOT NULL AUTO_INCREMENT,
    username   VARCHAR(40) NOT NULL,
    passHash   CHAR(64)    NOT NULL,
    wins       INT         NOT NULL CHECK (wins >= 0),
    games      INT         NOT NULL,
    avgWinTime DOUBLE      NOT NULL CHECK (avgWinTime >= 0),
    -- PRIMARY KEY (UserID),
    PRIMARY KEY (username),
    CHECK (games >= wins)  
);

CREATE TABLE OnlineUser(
    -- UserID INT,
    -- FOREIGN KEY(UserID) REFERENCES UserInfo(UserID)
    username VARCHAR(40) NOT NULL,
    FOREIGN KEY (username) REFERENCES UserInfo(username),
    PRIMARY KEY (username)
);

INSERT INTO UserInfo(username, passHash, wins, games, avgWinTime)
VALUES ('Shaheer', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 1, 10, 10.2);