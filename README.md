# <h1 align="center">Share It</h1>

<br/>

<h3 align="center">Service for renting things.</h3>

## Service features: 
<br/>:white_check_mark: The owner of a thing can create, update, delete a thing for sharing 
<br/>:white_check_mark: The user create a request to book a thing if it's available for a certain period
<br/>:white_check_mark: The owner can confirm the booking or reject 
<br/>:white_check_mark: The owner of things can view all things, see the last and the next booking 
<br/>:white_check_mark: Users can leave comments on the thing if they actually booked it and have already returned it to the owner 
<br/>:white_check_mark: Users can create a request for the thing they need. Owners of suitable items can add new items based on user requests 
<br/>:white_check_mark: Users can get a filtered list of items depending on the desired parameters
<br/>:white_check_mark: Owners of items can receive a list of reviews for the specified item

## The application is represented by two services: 
`server` is responsible for all business logic, `gateway` validates incoming data.

<br/>

</br>

The project is covered by the following tests: Unit Testing, Testing Spring MVC Web Controllers, JPA Queries, Integration Tests.

<hr>

## Want to run this project? ##
Create images and spin containers:

`$ docker-compose up -d --build`

After that,  gateway is available at http://localhost:8080/, server - at http://localhost:9090/.
If desired, it's possible to change the ports in the application.properties file

<hr>
