# README

The Project runs with Java 8 Spring Boot 2.3 and local file h2 database

Default server address is:

`localhost:8080`

Default DB url is:

`jdbc:h2:file:./h2/loan;AUTO_SERVER=TRUE`

The user need to log in through `/api/auth/signin` link. Postman collection is provided as attachment.

There is no need any privileges for login page but for registration request the user need **ADMIN** role

## APPLICATION PROPERTIES

* loan.api.create.test.data=true
* loan.api.installment.options=6,9,12,24
* loan.api.max.interest.rate=0.5
* loan.api.min.interest.rate=0.1
* loan.api.max.payable.months=3
* loan.api.daily.penalty.interest.rate=0.001

**loan.api.create.test.data** should be set to false for prod environment
when it is true DataLoader.class creates example customer and loan with installments for quick testing.

**loan.api.installment.options** is installment options split with comma.

**loan.api.max.interest.rate** and **loan.api.min.interest.rate** are the maximum and minimum interest rates (respectively) to apply to loans.

**loan.api.max.payable.months** is maximum months of installments to pay early.

**loan.api.daily.penalty.interest.rate** is the daily interest rate applies to loans that paid early or late, when a loan paid early amount is reduced, when it's paid late amount is increased. Can be set zero to have no effect.

## ENDPOINTS

### Login (POST)

`/api/auth/signin` Used to login into application. Returns an **accessToken** that used as a bearer token to use all services.  

Example Request Body:
`{
"username": "customer",
"password": "customer"
}`

### Register (POST)

`/api/auth/signup` Used to create new user (Admin or Customer)

Example Request Body:
`{
"username": "customer2",
"email": "customer2@loanapi.com",
"password": "customer2",
"role": ["customer"],
"name":"İkinci",
"surname":"Müşteri",
"creditLimit": 20000
}`

### Get Loans (GET)

`/api/loans/get` Brings all loans of a customer

### Get Loans for Customer (GET)

`/api/loans/get/{username}` Brings all loans of a certain customer. Admin role is needed to use this method.


### Get Loan Installments for Loan (GET)

`/api/loans/getInstallments/{id}` Brings all loan installments for a loan.

### Get All Loans (GET)

`/api/loans/getAll` Brings all loans. Admin role is needed

### Create Loan (POST)

`/api/loans/create` Creates a loan. username parameter added for only admin role users. this parameter specifies which customer gets the loan.

Example Request Body:
`{
"username": "customer",
"installments": 6,
"interestRate": 0.4,
"amount": 10000
}`

### Pay Loan (POST)

`/api/loans/pay` Used to pay a loan. username parameter added for only admin role users. this parameter specifies which customer pays the loan.

Example Request Body:
`{
"username": "customer",
"loanId": 1,
"amount": 5105
}`




