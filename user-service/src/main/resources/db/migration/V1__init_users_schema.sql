CREATE TABLE profiles (
    user_id UUID PRIMARY KEY,
    headline VARCHAR(255),
    summary TEXT,
    location VARCHAR(255),
    phone VARCHAR(50)
);

CREATE TABLE skills (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    level VARCHAR(50),
    years INTEGER,
    source VARCHAR(50) NOT NULL,
    UNIQUE (user_id, name)
);

CREATE TABLE educations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    degree VARCHAR(255) NOT NULL,
    institution VARCHAR(255) NOT NULL,
    field VARCHAR(255),
    start_date DATE,
    end_date DATE
);

CREATE TABLE experiences (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    company VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE
);

CREATE TABLE certifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    issuer VARCHAR(255),
    date DATE,
    verified BOOLEAN DEFAULT FALSE
);

CREATE TABLE preferences (
    user_id UUID PRIMARY KEY,
    minimum_salary DECIMAL(12,2)
);

CREATE TABLE preference_roles (
    preference_user_id UUID NOT NULL,
    role VARCHAR(255) NOT NULL,
    FOREIGN KEY (preference_user_id) REFERENCES preferences(user_id) ON DELETE CASCADE
);

CREATE TABLE preference_locations (
    preference_user_id UUID NOT NULL,
    location VARCHAR(255) NOT NULL,
    FOREIGN KEY (preference_user_id) REFERENCES preferences(user_id) ON DELETE CASCADE
);

CREATE TABLE preference_work_modes (
    preference_user_id UUID NOT NULL,
    work_mode VARCHAR(50) NOT NULL,
    FOREIGN KEY (preference_user_id) REFERENCES preferences(user_id) ON DELETE CASCADE
);
