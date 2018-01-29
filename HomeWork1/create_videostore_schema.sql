create table ZipCode
    (Code   varchar(10),
     City   varchar(20),
     State  varchar(15),
     primary key (Code)
    )

create table Customer
	(CID		int IDENTITY(1000,1),
	 Title		varchar(20),
	 FirstName  varchar(15),
	 LastName 	varchar(20),
	 Str_Nmb	int,
	 Str_Add	varchar(50),
	 Email		varchar(40),
	 PW			varchar(15),
	 Code		varchar(10),
	 MovieID	int
	 primary key (CID),
     foreign key (Code) references ZipCode
     on delete set null
	)

	create table Store
    (StoreID    int IDENTITY(1000,1),
     StreetNum  int,
     StreetAdd  varchar(30),
     PhoneNum   varchar(15),
     primary key (StoreID)
    )

create table Prefers
    (CID        integer,
    StoreID     integer,
    position    integer,
    primary key (CID, StoreID),
    unique (StoreID, position),
	foreign key (CID) references Customer,
	foreign key (StoreID) references Store
    )

create table PartOf
    (AreaID		 varchar(20),
     Code		 varchar,
     primary key (AreaID, Code)
    )
    
create table Area
    (AreaID    int IDENTITY(1000,1),
     Name      varchar(15),
     primary key (AreaID)
    )

create table Reserves
    (MovieID      int,
     MovieTitle   varchar(20),
     CID          int,
     primary key (MovieID,CID),
     foreign key (MovieID) references Movies,
     foreign key (CID) references Customer
    )
