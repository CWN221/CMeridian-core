package com.bank.desktop.controller;

import javafx.fxml.Initializable;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class AuditController implements Initializable {
    @Override public void initialize(URL location, ResourceBundle resources) {
        log.info("Audit log loaded");
    }
}