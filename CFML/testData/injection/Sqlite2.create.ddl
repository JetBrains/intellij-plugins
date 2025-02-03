create table users
(
	status VARCHAR(255),
	firstName VARCHAR(255),
	lastName VARCHAR(255),
	UserID int
)
;

create unique index users_UserID_uindex
	on users (UserID)
;