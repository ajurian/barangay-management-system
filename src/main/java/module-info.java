module barangay.management {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires transitive java.sql;
    requires jbcrypt;
    requires org.xerial.sqlitejdbc;
    requires kernel;
    requires layout;
    requires io;

    // Export and open presentation layer
    exports com.barangay.presentation;

    opens com.barangay.presentation to javafx.fxml;
    opens com.barangay.presentation.controllers to javafx.fxml;

    // Export domain layer
    exports com.barangay.domain.entities;
    exports com.barangay.domain.valueobjects;
    exports com.barangay.domain.exceptions;
    exports com.barangay.domain.repositories;

    // Export application layer
    exports com.barangay.application.dto;
    exports com.barangay.application.usecases;
    exports com.barangay.application.services;
    exports com.barangay.application.ports;

    // Export infrastructure layer
    exports com.barangay.infrastructure.persistence;
    exports com.barangay.infrastructure.security;
    exports com.barangay.infrastructure.config;
    exports com.barangay.infrastructure.services;
}
