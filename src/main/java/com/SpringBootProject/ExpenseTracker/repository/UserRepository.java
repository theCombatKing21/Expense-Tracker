package com.SpringBootProject.ExpenseTracker.repository;

import com.SpringBootProject.ExpenseTracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// @Repository marks this as a Spring-managed bean AND tells Spring to translate
// low-level database exceptions (like SQLExceptions) into Spring's own
// DataAccessException hierarchy. This decouples your service layer from JDBC details.
//
// JpaRepository<User, Long> gives you for FREE:
//   save(user), findById(id), findAll(), deleteById(id), count(), existsById(id)
//   and many more — all without writing a single line of SQL.
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA reads this method name and generates the SQL automatically:
    // SELECT * FROM users WHERE email = ?
    // This is called a "derived query method" — Spring parses "findBy" + "Email"
    // and maps it to the 'email' field on the User entity.
    Optional<User> findByEmail(String email);

    // Generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?
    // Useful for checking duplicates before creating a user.
    boolean existsByEmail(String email);
}