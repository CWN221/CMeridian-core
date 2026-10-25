package com.bank.desktop.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class SettingsController implements Initializable {

    @FXML private ComboBox<String> languageCombo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageCombo.setItems(FXCollections.observableArrayList(
                "English (Kenya)",
                "Kiswahili"
        ));
        languageCombo.getSelectionModel().selectFirst();
        log.info("Settings loaded");
    }
}