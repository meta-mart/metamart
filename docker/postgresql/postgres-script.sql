CREATE DATABASE metamart_db;
CREATE DATABASE airflow_db;
CREATE USER metamart_user WITH PASSWORD 'metamart_password';
CREATE USER airflow_user WITH PASSWORD 'airflow_pass';
ALTER DATABASE metamart_db OWNER TO metamart_user;
ALTER DATABASE airflow_db OWNER TO airflow_user;
ALTER USER airflow_user SET search_path = public;
commit;