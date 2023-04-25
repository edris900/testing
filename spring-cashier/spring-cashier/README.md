# CMPE 172 - Midterm Part 2

# Steps done to create Spring Cashier:

Dependencies needed:
 Spring Security, Spring Web, Spring Boot DevTools, Lombok, Validation, Spring Data JPA, MySQL Driver, Thymeleaf, Test Containers, Spring Data Reactive Redis,and Spring Data Redis


Paths Configuration Required:
Group: com.example, Artifact: spring-cashier, Name: spring-cashier, Package Name: come.example.spring-cashier, and Packaging: Jar

# Changes made to complete Spring Cashier:

### Explain (with code snippets from sample code) how the Web UI is able to "remember" the selected Store.
  * The Web UI is able to remember the selected store by using the "store" command. The store command is sent to the server and the server stores the store name in the session. The store name is then used to display the store name in the UI.
  * order.setRegister(command.getRegister());

### Explain (with code snippets from your code) how you implemented storing the Order in MySQL.
  * I implemented storing the Order in MySQL by using Spring JPA. I created a SpringQueryRepository interface that extends the JpaRepository interface. The JpaRepository interface provides methods for creating, reading, updating, and deleting data from the database. I then created a SpringQueryRepositoryImpl class that implements the SpringQueryRepository interface. The SpringQueryRepositoryImpl class uses the JpaRepository methods to create, read, update, and delete data from the database.

### Explain (with code snippets from your code) how you support generating a different "random" order with each "Place Order" request (instead of the starter code's "hard coded" order)
  * I support generating a different "random" order with each "Place Order" request by using the Random class. I created a Random object and used the nextInt method to generate a random number between 0 and 2. I then used the random number to select a random order from the orders array. I then used the random order to create a new Order object and return it.
  * Code Snippet:
    * Random random = new Random();
    * int randomOrder = random.nextInt(3);
    * Order order = orders[randomOrder];
    * return new Order(order.getStore(), order.getItems());
    
## Explain (with code snippets from your code) how you added support for Spring Security / Login Page
  * I added support for Spring Security / Login Page by creating a WebSecurityConfig class that extends the WebSecurityConfigurerAdapter class. The WebSecurityConfigurerAdapter class provides methods for configuring Spring Security. I then used the configure method to configure Spring Security. I used the inMemoryAuthentication method to create a new user with the username "user" and password "password". I then used the csrf method to disable CSRF protection.
### What's the User Name and Password you created? How did you "Hash" the Password?
  * The password is hashed using the BCrypt algorithm, which is the default password encoder specified in the passwordEncoder() method. Username is "user" and the Password generates new one every time the app is restarted.
### How does your implementation work behind a Load Balancer without using LB Sticky Sessions?
  * My implementation works behind a Load Balancer without using LB Sticky Sessions by using the HttpSession object. The HttpSession object stores the store name in the session. The store name is then used to display the st
### How does your solution managed "remembering" the active order for a register / store?
  * My solution managed "remembering" the active order for a register / store by using the HttpSession object. The HttpSession object stores the store name in the session. The store name is then used to display the store name in the UI. The HttpSession object also stores the order in the session. The order is then used to display the order in the UI.
  * But mainly, I have added Switch cases in my CashierController class in PostMapping section where it handles this situation.
### Did you make any System Architecture Changes to support the requirements? If Yes, please explain and include an updated System Architecture Diagram. You my use the starter diagram (spring-cashier.asta (https://sjsu.instructure.com/courses/1561209/files/72468409?wrap=1) (https://sjsu.instructure.com/courses/1561209/files/72468409/download?download_frd=1) ) and make changes to it.
  * Yes, I did make changes by adding Redis to it and removing Jumpbox, so that I can remove sticky sessions and make my load balancer working in Docker Desktop

# Steps done to complete Spring Cashier:

* Screenshots are added in zip folder
