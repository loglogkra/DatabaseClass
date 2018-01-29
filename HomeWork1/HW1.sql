use banking;
 
/*Question 1 Part (A)*/
select customer_name
from depositor 
except
select customer_name
from borrower

/*Question 1 Part (B)*/
select C.customer_name
from  customer C join customer Smith on 
C.customer_street=Smith.customer_street and C.customer_city = Smith.customer_city
where Smith.customer_name = 'Smith' and C.customer_name != 'Smith'

/*Question 1 Part (C)*/
select branch_name
from account
 inner join depositor on depositor.account_number = account.account_number
 inner join customer on customer.customer_name = depositor.customer_name 
where customer_city = 'Harrison'

/*Question 2 Part (A)*/
select distinct C.customer_name from depositor as C
where not exists (
 (select branch_name
  from branch
  where branch_city = 'Brooklyn')
  except
  (select B.branch_name
   from depositor as D, account as B
   where D.account_number = B.account_number and
         C.customer_name = D.customer_name ))

/*Question 2 Part (B)*/
select sum(amount)
from loan

/*Question 2 Part (C)*/
select branch_name
from branch
where assets > some
(select assets from branch
where branch_city = 'Brooklyn')

use [logan.kragt];

/*Question 3 Part (A)*/
select sum(points * credits) as total_points
from takes
inner join student on student.ID = takes.ID 
inner join grade_points on grade_points.grade = takes.grade
inner join course on course.course_id = takes.course_id
where student.ID = '12345';

/*Question 3 Part (B) */
select sum(credits * points) / sum(credits) as GPA
from takes 
inner join student on student.ID = takes.ID  
inner join grade_points on grade_points.grade = takes.grade  
inner join course on course.course_id = takes.course_id
where student.ID = '12345'

/*Question 3 Part (C) */
select student.ID, sum(credits * points)/sum(credits) as GPA
from takes 
inner join student on student.ID = takes.ID  
inner join course on course.course_id = takes.course_id
inner join grade_points on grade_points.grade= takes.grade 
group by student.ID, student.tot_cred
union
select ID, null as gpa
from student
where not exists
(select * from takes where takes.ID = student.ID)

/*Question 4 Part (A)*/
select distinct name
from student , takes, course
where course.course_id = takes.course_id and 
student.dept_name = course.dept_name and 
course.dept_name='Comp. Sci.'

/*Question 4 Part (B)*/
select ID, name
from student
except
select s.ID, name
from student as s, takes, course
where s.dept_name = course.dept_name and course.course_id = takes.course_id
and takes.year<2009

/*Question 4 Part (C)*/
select dept_name , max(salary)  as salary
from instructor 
group by dept_name;

/*Question 4 Part (D)*/
with maxSalary as 
(select max (salary) as maxSalary, dept_name
 from instructor 
 group by dept_name) 
select dept_name, salary
 from instructor where salary in 
	(select min(maxSalary)
	from maxSalary)
 
/*Question 5 Part (A)*/
insert into course(course_id,title,dept_name,credits) 
values ('CS-001', 'Weekly Seminar', 'Comp. Sci.', 0);

/*Question 5 Part (B)*/
insert into section(course_id, sec_id, semester, year) 
values('CS-001', '1', 'Fall', 2009);

/*Question 5 Part (C)*/
insert into takes(ID, course_id, sec_id, semester, year)
select ID, 'CS-001', '1', 'Fall', 2009
from student
where dept_name = 'Comp. Sci.';

/*Question 5 Part (D)*/
delete from takes
select S.ID, name
from student S, takes
where name = 'Chavez' and takes.course_id = 'CS-001' and takes.sec_id = '1' 
and semester = 'Fall' and year=2009

/*Question 5 Part(E)*/
delete from course
where course_id = 'CS-001';
--section has FK course_id and when we delete
--CS-001 from the course table which is tied to the section table, tuples
--in the section table that have 'CS-001' as their course_id will be deleted

/*Question 5 Part (F)*/
delete from takes
where course_id in (
	select course_id
	from course
	where lower(title) like '%database%');