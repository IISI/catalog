DROP TABLE JC_SCR_FILE;
DROP TABLE JC_BUILD_UNIT;
DROP TABLE JC_SCR;
DROP TABLE JC_APP_PATH;
DROP TABLE JC_APP;
DROP TABLE JC_PROGRAMMER;
DROP TABLE JC_COORDINATOR;

CREATE TABLE JC_APP (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    APP_ID VARCHAR(20) NOT NULL UNIQUE,
    DESCRIPTION NVARCHAR(256),
    PVCS_PROJ_DB VARCHAR(256) NOT NULL,
    PVCS_PROJ_PATH VARCHAR(256),
    CONSTRAINT UC_APP UNIQUE (PVCS_PROJ_DB, PVCS_PROJ_PATH)
);

CREATE TABLE JC_APP_PATH (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    JC_APP_ID INT NOT NULL REFERENCES JC_APP(ID) ON DELETE CASCADE,
    PATH_TYPE INT NOT NULL,
    PATH NVARCHAR(256) NOT NULL
);

CREATE TABLE JC_BUILD_UNIT (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    JC_APP_ID INT NOT NULL REFERENCES JC_APP(ID) ON DELETE CASCADE,
    UNIT_ID VARCHAR(256),
    CONSTRAINT UC_BUILD_UNIT UNIQUE (JC_APP_ID, UNIT_ID)
);

CREATE TABLE JC_BUILD_UNIT_PATH (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    JC_BUILD_UNIT_ID INT NOT NULL REFERENCES JC_BUILD_UNIT(ID) ON DELETE CASCADE,
    PATH_TYPE INT NOT NULL,
    PATH NVARCHAR(256) NOT NULL
);

CREATE TABLE JC_PROGRAMMER (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    TEAM NVARCHAR(32) NOT NULL,
    NAME NVARCHAR(32) NOT NULL,
    PHONE VARCHAR(20),
    MOBILE VARCHAR(20),
    CONSTRAINT UC_PROGRAMMER UNIQUE (TEAM, NAME)
);

CREATE TABLE JC_COORDINATOR (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    TEAM NVARCHAR(32) NOT NULL,
    NAME NVARCHAR(32) NOT NULL,
    PHONE VARCHAR(20),
    MOBILE VARCHAR(20),
    CONSTRAINT UC_COORDINATOR UNIQUE (TEAM, NAME)
);

CREATE TABLE JC_SCR (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    SCR_NO NVARCHAR(32) NOT NULL UNIQUE,
    JC_APP_ID INT NOT NULL REFERENCES JC_APP(ID),
    CREATE_TIME DATETIME NOT NULL,
    STATUS INT NOT NULL,
    PROCESS_TIME DATETIME NOT NULL,
    JC_COORDINATOR_ID INT NOT NULL REFERENCES JC_COORDINATOR(ID),
    JC_PROGRAMMER_ID INT NOT NULL REFERENCES JC_PROGRAMMER(ID),
    DESCRIPTION NVARCHAR(256),
    REGISTER_COUNT INT NOT NULL
);

CREATE TABLE JC_SCR_FILE (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    JC_SCR_ID INT NOT NULL REFERENCES JC_SCR(ID),
    FILE_PATH NVARCHAR(256) NOT NULL,
    FILE_NAME NVARCHAR(256) NOT NULL,
    JC_BUILD_UNIT_ID INT NOT NULL REFERENCES JC_BUILD_UNIT(ID),
    FILE_TYPE INT NOT NULL,
    CHECKOUT TINYINT,
    FILE_DATETIME DATETIME,
    FILE_SIZE BIGINT,
    FILE_MD5 VARCHAR(32),
    LAST_REGISTER_TIME DATETIME NOT NULL,
    LAST_COMPILE_TIME DATETIME
);
