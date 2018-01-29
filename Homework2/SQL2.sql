/*Question 1*/
select StageFirstName, StageLastName 
from Actors
	 inner join Roles on Roles.ActorID = Actors.ActorID
	 inner join Movies on Roles.MovieID = Movies.MovieID
where MovieTitle = 'Aladdin'
intersect
(select StageFirstName, StageLastName
from Actors
	inner join Roles on Roles.ActorID = Actors.ActorID
	inner join Movies on Movies.MovieID = Roles.MovieID
where MovieTitle = 'Patch Adams')
order by StageLastName ASC

/*Question 2*/
select Distinct A.StageFirstName, A.StageLastName, A.BirthCity 
from Actors A
inner join Actors B on A.BirthCity = B.DeathCity
order by StageLastName, StageFirstName

/*Question 3*/
Select StageFirstName, StageLastName, count(*) as NumberOfMovies
from Actors
inner join Roles on Actors.ActorID = Roles.ActorID
inner join Movies on Roles.MovieID = Movies.MovieID
where Director = 'Spielberg, Steven'
group by StageFirstName, StageLastName
having(count(*)>1)
order by count(*) DESC


/*Question 4 (3.16 Parts b-d)*/
--Create tables/populate
create table employee(
	employee_name	nvarchar(20) primary key,
	street			nvarchar(30),
	city			nvarchar(20),
	)
create table company(
	company_name	nvarchar(20) primary key,
	city			nvarchar(20),
	)
create table works(
	employee_name	nvarchar(20) references employee(employee_name) primary key,
	company_name	nvarchar(20) references company(company_name),
	salary			int
	) 
create table manages(
	employee_name	nvarchar(20) references employee(employee_name) primary key,
	manager_name	nvarchar(20)
	)

insert into employee values ('Logan Kragt', '20th St.', 'Holland');
insert into employee values ('Jim Cuatt', 'Tiffany Shores ct.', 'Holland');
insert into employee values ('Josh Gruppen', '20th St.', 'Holland');
insert into employee values ('Kim Williams', 'Wind Ln.', 'Zeeland');
insert into employee values ('Steve Bassett', 'James St.', 'Zeeland');
insert into employee values ('Dwight Gilbert', 'Bingham St.', 'Borculo');
insert into employee values ('Terry Gruppen', 'Cherry Ct.', 'Holland');
insert into employee values ('Marina Solis', 'Riley St.', 'Zeeland');
insert into employee values ('Lee Powell', 'Riley St.', 'Zeeland');
insert into employee values ('Don Borah', 'Riley St.', 'Holland');
insert into employee values ('Kathy Cole', '11 St.', 'Grand Rapids');
insert into employee values ('Tim Weidman', '20th St.', 'Grand Rapids');

insert into company values ('Artex', 'Zeeland');
insert into company values ('Gentex', 'Zeeland');

insert into works values ('Logan Kragt', 'Artex', 45000);
insert into works values ('Jim Cuatt', 'Gentex', 130000);
insert into works values ('Josh Gruppen', 'Artex', 90000);
insert into works values ('Kim Williams', 'Artex', 55000);
insert into works values ('Steve Bassett', 'Artex', 60000);
insert into works values ('Dwight Gilbert', 'Artex', 50000);
insert into works values ('Terry Gruppen', 'Artex', 250000);
insert into works values ('Marina Solis', 'Artex', 30000);
insert into works values ('Lee Powell', 'Artex', 50000);
insert into works values ('Don Borah', 'Artex', 55000);
insert into works values ('Kathy Cole', 'Gentex', 35000);
insert into works values ('Tim Weidman', 'Gentex', 28000);

insert into manages values ('Logan Kragt', 'Josh Gruppen');
insert into manages (employee_name) values ('Jim Cuatt');
insert into manages values ('Josh Gruppen', 'Terry Gruppen');
insert into manages values ('Kim Williams', 'Josh Gruppen');
insert into manages values ('Steve Bassett', 'Josh Gruppen');
insert into manages values ('Dwight Gilbert', 'Josh Gruppen');
insert into manages (employee_name) values ('Terry Gruppen');
insert into manages values ('Marina Solis', 'Josh Gruppen');
insert into manages values ('Lee Powell', 'Josh Gruppen');
insert into manages values ('Don Borah', 'Josh Gruppen');
insert into manages values ('Kathy Cole', 'Jim Cuatt');
insert into manages values ('Tim Weidman', 'Jim Cuatt');

/*Question 4 (3.16 Part B)*/
Select Distinct EMP.employee_name, EMP.city as EmployeeCity, Company.city as CompanyCity
from employee EMP
join works on works.employee_name = EMP.employee_name
join company on EMP.city = company.city

/*Question 4 (3.16 Part C)*/
Select EN1.employee_name, manager_name as Managed_By
from employee EN1, employee EN2, manages MG
where EN1.employee_name = MG.employee_name and
MG.manager_name = EN2.employee_name and EN1.street = EN2.street
and EN1.city = EN2.city

/*Question 4 (3.16 Part D)*/
select employee_name, company_name, salary
from works I
where salary > (select AVG(salary) from works II where I.company_name=II.company_name)
order by salary DESC








