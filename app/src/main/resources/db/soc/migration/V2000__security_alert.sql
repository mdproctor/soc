CREATE TABLE security_alert (
    alert_id        UUID            NOT NULL,
    source          VARCHAR(255)    NOT NULL,
    source_alert_id VARCHAR(255),
    alert_timestamp TIMESTAMP       NOT NULL,
    raw_payload     TEXT,
    severity        VARCHAR(20)     NOT NULL,
    category        VARCHAR(255),
    affected_assets TEXT,
    description     TEXT,
    received_at     TIMESTAMP       NOT NULL,
    tenancy_id      VARCHAR(255)    NOT NULL,

    CONSTRAINT pk_security_alert PRIMARY KEY (alert_id)
);

CREATE INDEX idx_security_alert_severity ON security_alert (severity);
CREATE INDEX idx_security_alert_source   ON security_alert (source);
CREATE INDEX idx_security_alert_timestamp ON security_alert (alert_timestamp);
CREATE INDEX idx_security_alert_tenancy  ON security_alert (tenancy_id);
